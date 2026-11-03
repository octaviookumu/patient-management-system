package com.octaviookumu.stack;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.msk.CfnCluster;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.route53.CfnHealthCheck;

import java.util.stream.Collectors;

public class LocalStack extends Stack {

    private final Vpc vpc;

    public LocalStack(final App scope, final String id, final StackProps props) {
        super(scope, id, props);

        this.vpc = createVpc();

        DatabaseInstance authServiceDb = createDatabase("AuthServiceDB", "auth-service-db");
        DatabaseInstance patientServiceDb = createDatabase("PatientServiceDB", "patient-service-db");

        CfnHealthCheck authDbHealthCheck = createDbHealthCheck(authServiceDb, "AuthServiceDbHealthCheck");
        CfnHealthCheck patientDbHealthCheck = createDbHealthCheck(patientServiceDb, "PatientServiceDbHealthCheck");

        CfnCluster mskCluster = createMskCluster();
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
