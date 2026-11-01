package com.octaviookumu.stack;

import software.amazon.awscdk.*;

public class LocalStack extends Stack {

    public LocalStack(final App scope, final String id, final StackProps props) {
        super(scope, id, props);
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
