package ua.ivan.epam.gym.application.bdd;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/component/gym")
@ConfigurationParameter(
        key = GLUE_PROPERTY_NAME,
        value = "ua.ivan.epam.gym.application.bdd"
)
@ConfigurationParameter(
        key = PLUGIN_PROPERTY_NAME,
        value = "pretty,"
                + "html:target/cucumber/component-gym.html,"
                + "json:target/cucumber/component-gym.json"
)
public class GymComponentCucumberTest {
}