import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class AuthIntegratedTest {
    // specify the baseUrl of all requests
    @BeforeAll // happens before all the tests run
    static void setUp() {
        RestAssured.baseURI = "http://localhost:4004";
    }

    @Test
    public void shouldReturnOKWithValidToken() {
        // 1. Arrange
        // 2. Act - given the body has a payload,
        // we're going to form something that triggers the things we are trying to test
        // 3. Assert
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

}
