package ua.ivan.epam.gym.application.bdd;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.*;

@RequiredArgsConstructor
public class ApiSteps {

    private final ApiScenarioState state;

    @LocalServerPort
    private int port;

    @Given("the request body is")
    public void requestBodyIs(String body) {
        state.setRequestBody(state.resolve(body));
    }

    @Given("no Bearer token is provided")
    public void noBearerTokenIsProvided() {
        state.setBearerToken(null);
    }

    @Given("I use the token stored as {string}")
    public void useStoredToken(String variableName) {
        state.setBearerToken(state.getRequired(variableName));
    }

    @When("I send GET request to {string}")
    public void sendGet(String path) {
        state.setResponse(
                request().get(state.resolve(path))
        );
    }

    @When("I send POST request to {string}")
    public void sendPost(String path) {
        state.setResponse(
                request()
                        .body(state.getRequestBody())
                        .post(state.resolve(path))
        );
    }

    @When("I send DELETE request to {string}")
    public void sendDelete(String path) {
        state.setResponse(
                request().delete(state.resolve(path))
        );
    }

    @Then("the response status is {int}")
    public void responseStatusIs(int expectedStatus) {
        assertEquals(
                expectedStatus,
                state.getResponse().statusCode(),
                state.getResponse().asPrettyString()
        );
    }

    @And("I save JSON field {string} as {string}")
    public void saveJsonField(String jsonPath, String variableName) {
        String value = state.getResponse()
                .jsonPath()
                .getString(jsonPath);

        assertNotNull(value, "JSON field is null: " + jsonPath);

        state.save(variableName, value);
    }

    @And("JSON string {string} equals {string}")
    public void jsonStringEquals(String jsonPath, String expected) {
        String actual = state.getResponse()
                .jsonPath()
                .getString(jsonPath);

        assertEquals(state.resolve(expected), actual);
    }

    @And("JSON number {string} equals {int}")
    public void jsonNumberEquals(String jsonPath, int expected) {
        Number actual = state.getResponse()
                .jsonPath()
                .get(jsonPath);

        assertNotNull(actual);
        assertEquals(expected, actual.intValue());
    }

    @And("JSON array {string} has size {int}")
    public void jsonArrayHasSize(String jsonPath, int expectedSize) {
        assertEquals(
                expectedSize,
                state.getResponse()
                        .jsonPath()
                        .getList(jsonPath)
                        .size()
        );
    }

    @And("the response body contains {string}")
    public void responseBodyContains(String expected) {
        assertTrue(
                state.getResponse()
                        .asString()
                        .contains(state.resolve(expected))
        );
    }

    private RequestSpecification request() {
        RequestSpecification specification = RestAssured
                .given()
                .baseUri("http://localhost")
                .port(port)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);

        if (state.getBearerToken() != null) {
            specification.header(
                    "Authorization",
                    "Bearer " + state.getBearerToken()
            );
        }

        return specification;
    }
}