package com.octaviookumu.api_gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class JwtValidationException {

    // This is needed when the api-gateway tries to call the validation endpoint on the auth-service
    // If the auth-service returns an unauthorised response, instead of returning a 500 from api-gateway
    // We intercept this and ensure the response from api-gateway will be unauthorised (401)

    // the Mono object is used by the filter chain to tell Spring that the current filter is finished
    @ExceptionHandler(WebClientResponseException.Unauthorized.class)
    public Mono<Void> handleUnauthorisedException(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
