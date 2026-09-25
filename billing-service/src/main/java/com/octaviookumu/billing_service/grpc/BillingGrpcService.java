package com.octaviookumu.billing_service.grpc;

import billing.BillingResponse;
import billing.BillingServiceGrpc.BillingServiceImplBase;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
public class BillingGrpcService extends BillingServiceImplBase {

    /**
     * @param billingRequest   comes from the generated code
     * @param responseObserver helps us get multiple responses, accept back-and-forth communication to client
     */
    @Override
    public void createBillingAccount(
            billing.BillingRequest billingRequest,
            StreamObserver<BillingResponse> responseObserver
    ) {
        // helps see our server receiving requests
        log.info("createBillingAccount request received {}", billingRequest.toString());

        // Business logic e.g. save to database, perform calculations

        BillingResponse response = BillingResponse.newBuilder()
                .setAccountId("12345")
                .setStatus("ACTIVE")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
