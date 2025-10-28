package org.example;

import io.quarkus.test.junit.QuarkusTest;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;
import org.junit.Test;

@QuarkusTest
public class TournamentApplicationTest {

    @Test
    public void testListAllPlayers() {
        given()
            .when().get("/players")
            .then()
            .statusCode(200)
            .body(
                containsString("Nuno"),
                containsString("João"),
                containsString("Fábio"));

        //Delete Nuno:
        given()
            .when().delete("/players/1")
            .then()
            .statusCode(204);

        //List all, Nuno should be missing now:
        given()
            .when().get("/players")
            .then()
            .statusCode(200)
            .body(
                not(containsString("Nuno")),
                containsString("João"),
                containsString("Fábio"));

        //Create Massas:
        given()
            .when()
            .body("{\"name\" : \"Massas\"}")
            .contentType("application/json")
            .post("/players")
            .then()
            .statusCode(201);

        //List all, Nuno should be missing now:
        given()
            .when().get("/players")
            .then()
            .statusCode(200)
            .body(
                not(containsString("Nuno")),
                containsString("João"),
                containsString("Fábio"),
                containsString("Massas"));
    }
}

