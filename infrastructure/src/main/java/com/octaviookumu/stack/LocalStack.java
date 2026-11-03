package com.octaviookumu.stack;

import io.github.cdimascio.dotenv.Dotenv;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.Protocol;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.msk.CfnCluster;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.route53.CfnHealthCheck;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalStack extends Stack {

    private final Vpc vpc;
    private final Cluster ecsCluster;

    private static final Dotenv ENV = Dotenv.load();

    public LocalStack(final App scope, final String id, final StackProps props) {
        super(scope, id, props);

        String jwtSecret = Objects.requireNonNull(
                ENV.get("JWT_SECRET")
        );

        this.vpc = createVpc();

        DatabaseInstance authServiceDb = createDatabase("AuthServiceDB", "auth-service-db");
        DatabaseInstance patientServiceDb = createDatabase("PatientServiceDB", "patient-service-db");

        CfnHealthCheck authDbHealthCheck = createDbHealthCheck(authServiceDb, "AuthServiceDbHealthCheck");
        CfnHealthCheck patientDbHealthCheck = createDbHealthCheck(patientServiceDb, "PatientServiceDbHealthCheck");

        CfnCluster mskCluster = createMskCluster();

        this.ecsCluster = createEcsCluster();

        // create services
        FargateService authService =
                createFargateService(
                        "AuthService",
                        "auth-service",
                        List.of(4005),
                        authServiceDb,
                        Map.of("JWT_SECRET", jwtSecret)
                );

        // tells cdk logic and cloudformation template that authService has a dependency on db and healthcheck
        // makes sure the db starts before we start the auth service
        authService.getNode().addDependency(authDbHealthCheck);
        authService.getNode().addDependency(authService);

        FargateService billingService =
                createFargateService(
                        "BillingService",
                        "billing-service",
                        List.of(4001, 9001),
                        null,
                        null
                );

        FargateService analyticsService =
                createFargateService(
                        "AnalyticsService",
                        "analytics-service",
                        List.of(4002),
                        null,
                        null
                );

        // has a dependency on kafka cluster
        analyticsService.getNode().addDependency(mskCluster);

        FargateService patientService =
                createFargateService(
                        "PatientService",
                        "patient-service",
                        List.of(4000),
                        patientServiceDb,
                        Map.of(
                                "BILLING_SERVICE_ADDRESS", "host.docker.internal",
                                "BILLING_SERVICE_GRPC_PORT", "9001"
                        )
                );

        patientService.getNode().addDependency(patientServiceDb);
        patientService.getNode().addDependency(patientDbHealthCheck);
        patientService.getNode().addDependency(billingService);
        patientService.getNode().addDependency(mskCluster);

        createApiGatewayService(); // add to cloudformation
    }

    private Vpc createVpc() {
        return Vpc.Builder.create(this, "PatientManagementVPC")
                .vpcName("PatientManagementVPC")
                .maxAzs(2)
                .build();
    }

    // create an RDS database
    private DatabaseInstance createDatabase(String id, String dbName) {
        return DatabaseInstance.Builder
                .create(this, id)
                .engine(DatabaseInstanceEngine.postgres(
                        PostgresInstanceEngineProps.builder()
                                .version(PostgresEngineVersion.VER_17_2)
                                .build()
                ))
                .vpc(vpc) // connect database to our vpc
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO)) // cpu, compute power, storage etc for db to run on
                .allocatedStorage(20)
                .storageEncrypted(true)
                .credentials(Credentials.fromGeneratedSecret("admin_user"))
                .databaseName(dbName)
                .removalPolicy(RemovalPolicy.DESTROY) // would do this in prod unless a backup is specified
                .build();
    }

    // create DB health check
    private CfnHealthCheck createDbHealthCheck(DatabaseInstance db, String id) {
        return CfnHealthCheck.Builder.create(this, id)
                .healthCheckConfig(CfnHealthCheck.HealthCheckConfigProperty.builder()
                        .type("TCP")
                        .port(Token.asNumber(db.getDbInstanceEndpointPort()))
                        .ipAddress(db.getDbInstanceEndpointAddress())
                        .requestInterval(30)
                        .failureThreshold(3)
                        .build())
                .build();
    }

    // create a kafka cluster using msk
    private CfnCluster createMskCluster() {
        return CfnCluster.Builder.create(this, "MskCluster")
                .clusterName("kafka-cluster")
                .kafkaVersion("3.2.0")
                .numberOfBrokerNodes(1)
                .brokerNodeGroupInfo(CfnCluster.BrokerNodeGroupInfoProperty.builder()
                        .instanceType("kafka.m5.xlarge")
                        .clientSubnets(vpc.getPrivateSubnets().stream() // get all private subnets from vpc
                                .map(ISubnet::getSubnetId)
                                .collect(Collectors.toList()))
                        .brokerAzDistribution("DEFAULT") // what brokers belong to what availability zones
                        .build()
                ).build();
    }

    private Cluster createEcsCluster() {
        return Cluster.Builder.create(this, "PatientManagementCluster")
                .vpc(vpc) // connect cluster to vpc
                .defaultCloudMapNamespace(CloudMapNamespaceOptions.builder()
                        .name("patient-management.local") // allows microservices to find and communicate using domain
                        // this means we don't need to know the IPs and internal addresses of our ECS services
                        // it is all managed by the cloud map service discovery
                        .build())
                .build();
    }

    /**
     * @param id                id
     * @param imageName         takes imageName and builds container from it
     * @param ports             what we want our containers to expose
     * @param db                if the service needs to connect to a database
     * @param additionalEnvVars variables needed by our service
     * @return FargateService - makes it easy start, stop and scale ecs tasks from the different containers
     */
    private FargateService createFargateService(
            String id,
            String imageName,
            List<Integer> ports,
            DatabaseInstance db,
            Map<String, String> additionalEnvVars
    ) {
        // an ecs task - it runs a container
        FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder
                .create(this, id + "Task")
                .cpu(256)
                .memoryLimitMiB(512)
                .build();

        // container options - used ContainerDefinitionOptions.Builder as I needed to add envVars to it below
        ContainerDefinitionOptions.Builder containerOptions = ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromRegistry(imageName)) // we want the container to be created from. link image to container
                .portMappings(ports.stream()
                        .map(port -> PortMapping.builder()
                                .containerPort(port) // port the app is running on inside container
                                .hostPort(port) // port we want to expose for other services
                                .protocol(Protocol.TCP)
                                .build())
                        .toList())
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder() // how we want logs displayed and where they go to
                        .logGroup(LogGroup.Builder.create(this, id + "LogGroup") // group a given container's logs
                                .logGroupName("/ecs/" + imageName)
                                .removalPolicy(RemovalPolicy.DESTROY) // when the stack is destroyed, destroy the logs
                                .retention(RetentionDays.ONE_DAY)
                                .build())
                        .streamPrefix(imageName)
                        .build()));

        // define env variables
        Map<String, String> envVars = new HashMap<>();
        envVars.put("SPRING_KAFKA_BOOTSTRAP_SERVERS",
                "localhost.localstack.cloud.4510, localhost.localstack.cloud.4511, localhost.localstack.cloud.4512");

        if (additionalEnvVars != null) {
            envVars.putAll(additionalEnvVars);
        }

        // specify connection string for service to connect to db
        if (db != null) {
            envVars.put("SPRING_DATASOURCE_URL", "jdbc:postgresql://%s:%s/%s-db".formatted(
                    db.getDbInstanceEndpointAddress(),
                    db.getDbInstanceEndpointPort(),
                    imageName
            ));

            envVars.put("SPRING_DATASOURCE_USERNAME", "admin_user");
            envVars.put("SPRING_DATASOURCE_PASSWORD",
                    Objects.requireNonNull(db.getSecret()).secretValueFromJson("password").toString());
            envVars.put("SPRING_JPA_HIBERNATE_DDL_AUTO", "update");
            envVars.put("SPRING_SQL_INIT_MODE", "always");
            envVars.put("SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT", "600000");
        }

        containerOptions.environment(envVars);
        // add/link new container to taskDefinition
        taskDefinition.addContainer(imageName + "Container", containerOptions.build());

        // create a FargateService that will run the ecs task
        return FargateService.Builder.create(this, id)
                .cluster(ecsCluster)
                .taskDefinition(taskDefinition)
                .assignPublicIp(false)
                .serviceName(imageName)
                .build();
    }

    // even though the API gateway is a FargateService, it is a special case in that
    // we want it to be a load balanced service as well (view diagram)
    private void createApiGatewayService() {
        FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder
                .create(this, "ApiGatewayTaskDefinition")
                .cpu(256)
                .memoryLimitMiB(512)
                .build();

        ContainerDefinitionOptions containerOptions = ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromRegistry("api-gateway"))
                .environment(Map.of(
                        "SPRING_PROFILES_ACTIVE", "prod",
                        "AUTH_SERVICE_URL", "http://host.docker.internal:4005"
                )) // because Localstack doesn't implement service discovery very well,
                // we use the docker internal service discovery in combination with the port so the services can find each other
                .portMappings(Stream.of(4004)
                        .map(port -> PortMapping.builder()
                                .containerPort(port) // port the app is running on inside container
                                .hostPort(port) // port we want to expose for other services
                                .protocol(Protocol.TCP)
                                .build())
                        .toList())
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                        .logGroup(LogGroup.Builder.create(this, "ApiGatewayLogGroup")
                                .logGroupName("/ecs/api-gateway")
                                .removalPolicy(RemovalPolicy.DESTROY)
                                .retention(RetentionDays.ONE_DAY)
                                .build())
                        .streamPrefix("api-gateway")
                        .build()))
                .build();

        // link container options to task definition
        taskDefinition.addContainer("ApiGatewayContainer", containerOptions);

        // will automatically create a load balancer for us - provides entry point to protected infrastructure
        ApplicationLoadBalancedFargateService apiGateway =
                ApplicationLoadBalancedFargateService.Builder.create(this, "ApiGatewayService")
                        .cluster(ecsCluster)
                        .serviceName("api-gateway")
                        .taskDefinition(taskDefinition)
                        .desiredCount(1)
                        .healthCheckGracePeriod(Duration.seconds(60)) // how long the ALB will wait for ApiGateway container to start before throwing an error
                        .build();

    }

    // entry point for code that will create cloudformation template for our infrastructure
    public static void main(final String[] args) {
        // create a new cdk app
        App app = new App(AppProps.builder().outdir("./cdk.out").build());

        Validations.of(app).acknowledge(
                Acknowledgment.builder()
                        .id("CloudFormation-Validate::F0001")
                        .reason("The starter stack is intentionally empty.")
                        .build()
        );

        // define additional properties to apply to stack
        // BootstraplessSynthesizer() - skip the initial bootstrapping of the cdk environment as we don't need it for Localstack
        StackProps props = StackProps.builder()
                .synthesizer(new BootstraplessSynthesizer()) // convert our code to cloudformation template
                .build();
        // link Localstack class to app
        new LocalStack(app, "localstack", props);

        System.out.println("Synthesizing app...");
        app.synth();
        System.out.println("Synthesis complete.");
    }
}
