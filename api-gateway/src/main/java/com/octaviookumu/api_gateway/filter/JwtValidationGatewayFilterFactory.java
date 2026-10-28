package com.octaviookumu.api_gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

// a filter class that allows us to intercept http requests, apply custom logic
// and decide whether to continue processing or to cancel the request
@Component
public class JwtValidationGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    // will use this to make http requests to auth service
    private final WebClient webClient;

    public JwtValidationGatewayFilterFactory(
            WebClient.Builder webClientBuilder,
            @Value("${auth.service.url}") String authServiceUrl
    ) {
        this.webClient = webClientBuilder.baseUrl(authServiceUrl).build();
    }


    @Override
    public GatewayFilter apply(Object config) {
        // exchange - an object passed by spring gateway that holds properties of the current request
        // chain - the chain of filters that currently exists in the filter chain
        return (exchange, chain) -> {
            // check the request for an authorisation token
            String token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            // check if token exists and is in the right format
            if (token == null || !token.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // call the validate endpoint on the auth service to let it do its validation
            return webClient.get()
                    .uri("/api/v1/auth/validate")
                    .header(HttpHeaders.AUTHORIZATION, token)
                    .retrieve()
                    .toBodilessEntity()  // tell the webClient the response doesn't have a body
                    .then(chain.filter(exchange)); // continue the request down the chain or its destination
        };
    }
}
