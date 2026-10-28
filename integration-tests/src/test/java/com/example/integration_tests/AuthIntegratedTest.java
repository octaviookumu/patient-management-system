package com.example.integration_tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class AuthIntegratedTest {
    // specify the baseUrl of all requests. happens before all the tests run
    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "http://localhost:4004";
    }

    @Test
    public void shouldReturnOKWithValidToken() {
        String loginPayload = """
                    {
                        "email": "testuser@test.com",
                        "password": "password123"
                    }
                """;

        Response response = given() // act
                .contentType("application/json")
                .body(loginPayload)
                .when() // act
                .post("/api/v1/auth/login")
                .then() // assert
                .statusCode(200) // do the things we want to assert on
                .body("token", notNullValue())
                .extract() // pull the response from the request
                .response(); // get the actual response object and assign it

        System.out.println("Generated Token: " + response.jsonPath().getString("token"));
    }

    @Test
    public void shouldReturnUnauthorizedOnInvalidLogin() {
        String loginPayload = """
                    {
                        "email": "invalid_user@test.com",
                        "password": "wrongemail345"
                    }
                """;

        given() // act
                .contentType("application/json")
                .body(loginPayload)
                .when() // act
                .post("/api/v1/auth/login")
                .then() // assert
                .statusCode(401); // do the things we want to assert on
    }

}
