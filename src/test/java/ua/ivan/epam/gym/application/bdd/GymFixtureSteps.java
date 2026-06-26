package ua.ivan.epam.gym.application.bdd;

import io.cucumber.java.en.Given;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RequiredArgsConstructor
public class GymFixtureSteps {

    private final ApiScenarioState state;

    @LocalServerPort
    private int port;

    @Given("a trainee and trainer are registered and the trainee is authenticated")
    public void registerProfilesAndAuthenticate() {
        String runId = state.getRequired("runId");

        Response traineeRegistration = post(
                "/api/v1/trainees",
                """
                {
                  "firstName": "Trainee%s",
                  "lastName": "Component",
                  "dateOfBirth": "2000-05-10",
                  "address": "London"
                }
                """.formatted(runId)
        );

        assertEquals(200, traineeRegistration.statusCode());

        String traineeUsername =
                traineeRegistration.jsonPath().getString("username");
        String traineePassword =
                traineeRegistration.jsonPath().getString("password");

        Response trainerRegistration = post(
                "/api/v1/trainers",
                """
                {
                  "firstName": "Trainer%s",
                  "lastName": "Component",
                  "specializationId": 1
                }
                """.formatted(runId)
        );

        assertEquals(200, trainerRegistration.statusCode());

        String trainerUsername =
                trainerRegistration.jsonPath().getString("username");

        Response login = post(
                "/api/v1/login",
                """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(traineeUsername, traineePassword)
        );

        assertEquals(200, login.statusCode());

        state.save("traineeUsername", traineeUsername);
        state.save("traineePassword", traineePassword);
        state.save("trainerUsername", trainerUsername);
        state.save("token", login.jsonPath().getString("token"));
        state.setBearerToken(login.jsonPath().getString("token"));
    }

    private Response post(String path, String body) {
        return RestAssured.given()
                .baseUri("http://localhost")
                .port(port)
                .contentType(ContentType.JSON)
                .body(body)
                .post(path);
    }
}