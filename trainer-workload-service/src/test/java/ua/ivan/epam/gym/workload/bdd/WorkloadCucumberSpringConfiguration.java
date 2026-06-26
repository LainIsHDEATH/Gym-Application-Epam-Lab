package ua.ivan.epam.gym.workload.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("component-test")
public class WorkloadCucumberSpringConfiguration {

    private static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7.0");

    static {
        MONGO.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.data.mongodb.uri",
                MONGO::getReplicaSetUrl
        );

        registry.add(
                "spring.data.mongodb.auto-index-creation",
                () -> true
        );

        registry.add(
                "spring.activemq.broker-url",
                () -> "tcp://localhost:61616"
        );

        registry.add(
                "spring.activemq.user",
                () -> "test"
        );

        registry.add(
                "spring.activemq.password",
                () -> "test"
        );

        registry.add("eureka.client.enabled", () -> false);
        registry.add("spring.cloud.discovery.enabled", () -> false);
        registry.add("spring.jms.listener.auto-startup", () -> false);

        registry.add(
                "security.jwt.secret",
                () -> "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
        );
    }
}