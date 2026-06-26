package ua.ivan.epam.gym.workload.bdd;

import io.cucumber.spring.ScenarioScope;
import io.restassured.response.Response;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@ScenarioScope
@Getter
@Setter
public class ApiScenarioState {

    private final Map<String, String> values = new HashMap<>();

    private String requestBody;
    private String bearerToken;
    private Response response;

    public ApiScenarioState() {
        values.put(
                "runId",
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 8)
        );
    }

    public void save(String name, String value) {
        values.put(name, value);
    }

    public String getRequired(String name) {
        String value = values.get(name);

        if (value == null) {
            throw new IllegalStateException("Scenario value is not defined: " + name);
        }

        return value;
    }

    public String resolve(String text) {
        String resolved = text;

        for (Map.Entry<String, String> entry : values.entrySet()) {
            resolved = resolved.replace(
                    "${" + entry.getKey() + "}",
                    entry.getValue()
            );
        }

        return resolved;
    }
}
