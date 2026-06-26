package ua.ivan.epam.gym.workload.bdd;

import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WorkloadSecuritySteps {

    private final ApiScenarioState state;
    private final TestJwtFactory testJwtFactory;

    @Given("I use a valid service token")
    public void useServiceToken() {
        state.setBearerToken(testJwtFactory.createServiceToken());
    }

    @Given("I use a valid user token")
    public void useUserToken() {
        state.setBearerToken(testJwtFactory.createUserToken());
    }
}