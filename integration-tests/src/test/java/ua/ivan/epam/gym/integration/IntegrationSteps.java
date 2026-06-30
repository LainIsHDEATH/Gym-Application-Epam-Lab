package ua.ivan.epam.gym.integration;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class IntegrationSteps {

    private static final String GYM_BASE_URL =
            System.getProperty(
                    "integration.gym-base-url",
                    "http://localhost:18080"
            );

    private static final String WORKLOAD_BASE_URL =
            System.getProperty(
                    "integration.workload-base-url",
                    "http://localhost:18081"
            );

    private static final String POSTGRES_URL =
            System.getProperty(
                    "integration.postgres-url",
                    "jdbc:postgresql://localhost:15431/gym_db"
            );

    private static final String POSTGRES_USERNAME =
            System.getProperty(
                    "integration.postgres-username",
                    "postgres"
            );

    private static final String POSTGRES_PASSWORD =
            System.getProperty(
                    "integration.postgres-password",
                    "postgres"
            );

    private static final String JWT_SECRET =
            System.getProperty(
                    "integration.jwt-secret",
                    "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
            );

    private String traineeUsername;
    private String traineePassword;
    private String trainerUsername;
    private String traineeToken;

    private String trainingName;
    private Long trainingId;

    private Response lastGymResponse;

    @Given("a trainee and trainer are registered through Gym Application")
    public void registerTraineeAndTrainer() {
        String runId = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8);

        trainingName = "BDD Integration " + runId;

        Response traineeRegistration = RestAssured.given()
                .baseUri(GYM_BASE_URL)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "firstName": "Trainee%s",
                          "lastName": "Integration",
                          "dateOfBirth": "2000-05-10",
                          "address": "Kyiv"
                        }
                        """.formatted(runId))
                .post("/api/v1/trainees");

        assertEquals(
                200,
                traineeRegistration.statusCode(),
                traineeRegistration.asPrettyString()
        );

        traineeUsername = traineeRegistration.jsonPath().getString("username");

        traineePassword = traineeRegistration.jsonPath().getString("password");

        assertNotNull(traineeUsername);
        assertNotNull(traineePassword);

        Response trainerRegistration = RestAssured.given()
                .baseUri(GYM_BASE_URL)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "firstName": "Trainer%s",
                          "lastName": "Integration",
                          "specializationId": 1
                        }
                        """.formatted(runId))
                .post("/api/v1/trainers");

        assertEquals(
                200,
                trainerRegistration.statusCode(),
                trainerRegistration.asPrettyString()
        );

        trainerUsername = trainerRegistration.jsonPath().getString("username");

        assertNotNull(trainerUsername);
    }

    @Given("the trainee is authenticated")
    public void authenticateTrainee() {
        Response loginResponse = RestAssured.given()
                .baseUri(GYM_BASE_URL)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """.formatted(
                        traineeUsername,
                        traineePassword
                ))
                .post("/api/v1/login");

        assertEquals(
                200,
                loginResponse.statusCode(),
                loginResponse.asPrettyString()
        );

        traineeToken = loginResponse.jsonPath().getString("token");

        assertNotNull(traineeToken);
    }

    @When("a {int} minute training is created for date {string}")
    public void createTraining(int duration, String trainingDate) {
        lastGymResponse = submitTraining(duration, trainingDate);
    }

    @When("a {int} minute training is submitted for date {string}")
    public void submitPossiblyInvalidTraining(
            int duration,
            String trainingDate
    ) {
        lastGymResponse = submitTraining(duration, trainingDate);
    }

    @Given("a {int} minute training exists for date {string}")
    public void existingTraining(
            int duration,
            String trainingDate
    ) {
        lastGymResponse = submitTraining(duration, trainingDate);

        assertEquals(
                200,
                lastGymResponse.statusCode(),
                lastGymResponse.asPrettyString()
        );

        trainingId = findCreatedTrainingId();

        assertNotNull(trainingId);
    }

    @Given(
            "trainer workload eventually equals {int} for year {int} and month {int}"
    )
    public void workloadEventuallyEquals(
            int expectedDuration,
            int year,
            int month
    ) {
        awaitWorkload(expectedDuration, year, month);
    }

    @When("the training is cancelled")
    public void cancelTraining() {
        assertNotNull(
                trainingId,
                "Training ID must be loaded before cancellation"
        );

        lastGymResponse = RestAssured.given()
                .baseUri(GYM_BASE_URL)
                .auth()
                .oauth2(traineeToken)
                .accept(ContentType.JSON)
                .delete(
                        "/api/v1/trainings/{id}",
                        trainingId
                );
    }

    @Then("Gym Application returns status {int}")
    public void gymApplicationReturnsStatus(int expectedStatus) {
        assertNotNull(
                lastGymResponse,
                "Gym Application response is not initialized"
        );

        assertEquals(
                expectedStatus,
                lastGymResponse.statusCode(),
                lastGymResponse.asPrettyString()
        );
    }

    @Then(
            "eventually trainer workload for year {int} and month {int} equals {int}"
    )
    public void eventuallyWorkloadEquals(
            int year,
            int month,
            int expectedDuration
    ) {
        awaitWorkload(expectedDuration, year, month);
    }

    @Then("trainer workload is not created")
    public void workloadIsNotCreated() {
        String serviceToken = createServiceToken();
        await()
                .during(Duration.ofSeconds(2))
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(250))
                .untilAsserted(() -> {
                    Response workloadResponse =
                            getTrainerWorkload(serviceToken);

                    assertEquals(
                            404,
                            workloadResponse.statusCode(),
                            workloadResponse.asPrettyString()
                    );
                });
    }

    private Response submitTraining(
            int duration,
            String trainingDate
    ) {
        assertNotNull(
                traineeToken,
                "Trainee must be authenticated before creating training"
        );

        return RestAssured.given()
                .baseUri(GYM_BASE_URL)
                .auth()
                .oauth2(traineeToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("""
                        {
                          "traineeUsername": "%s",
                          "trainerUsername": "%s",
                          "trainingName": "%s",
                          "trainingDate": "%s",
                          "trainingDuration": %d
                        }
                        """.formatted(
                        traineeUsername,
                        trainerUsername,
                        trainingName,
                        trainingDate,
                        duration
                ))
                .post("/api/v1/trainings");
    }

    private void awaitWorkload(
            int expectedDuration,
            int year,
            int month
    ) {
        String serviceToken = createServiceToken();

        await()
                .atMost(Duration.ofSeconds(20))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    Response workloadResponse =
                            getMonthlyWorkload(
                                    serviceToken,
                                    year,
                                    month
                            );

                    assertEquals(
                            200,
                            workloadResponse.statusCode(),
                            workloadResponse.asPrettyString()
                    );

                    Number actualDuration = workloadResponse
                            .jsonPath()
                            .get("trainingSummaryDuration");

                    assertNotNull(
                            actualDuration,
                            workloadResponse.asPrettyString()
                    );

                    assertEquals(
                            expectedDuration,
                            actualDuration.intValue(),
                            workloadResponse.asPrettyString()
                    );
                });
    }

    private Response getMonthlyWorkload(
            String serviceToken,
            int year,
            int month
    ) {
        return RestAssured.given()
                .baseUri(WORKLOAD_BASE_URL)
                .auth()
                .oauth2(serviceToken)
                .accept(ContentType.JSON)
                .queryParam("year", year)
                .queryParam("month", month)
                .get(
                        "/api/v1/trainers/{username}/workloads/monthly",
                        trainerUsername
                );
    }

    private Response getTrainerWorkload(String serviceToken) {
        return RestAssured.given()
                .baseUri(WORKLOAD_BASE_URL)
                .auth()
                .oauth2(serviceToken)
                .accept(ContentType.JSON)
                .get(
                        "/api/v1/trainers/{username}/workloads",
                        trainerUsername
                );
    }

    private Long findCreatedTrainingId() {
        String sql = """
                SELECT id
                FROM trainings
                WHERE training_name = ?
                ORDER BY id DESC
                LIMIT 1
                """;

        try (
                Connection connection = DriverManager.getConnection(
                        POSTGRES_URL,
                        POSTGRES_USERNAME,
                        POSTGRES_PASSWORD
                );

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, trainingName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException(
                            "Created training was not found in PostgreSQL. "
                                    + "trainingName=" + trainingName
                    );
                }

                return resultSet.getLong("id");
            }
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to load created training ID",
                    exception
            );
        }
    }

    private String createServiceToken() {
        try {
            Instant now = Instant.now();

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject("integration-tests")
                    .issueTime(Date.from(now))
                    .expirationTime(
                            Date.from(now.plusSeconds(300))
                    )
                    .claim(
                            "authorities",
                            List.of("ROLE_SERVICE")
                    )
                    .build();

            SignedJWT jwt = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claims
            );

            byte[] secret = Base64
                    .getDecoder()
                    .decode(JWT_SECRET);

            jwt.sign(new MACSigner(secret));

            return jwt.serialize();
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to generate service JWT",
                    exception
            );
        }
    }
}