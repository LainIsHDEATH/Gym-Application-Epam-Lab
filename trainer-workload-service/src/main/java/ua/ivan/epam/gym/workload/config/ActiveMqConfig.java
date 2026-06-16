package ua.ivan.epam.gym.workload.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.jms.ConnectionFactory;

@Configuration
public class ActiveMqConfig {

    @Bean
    public ConnectionFactory connectionFactory(
            @Value("${spring.activemq.broker-url}") String brokerUrl,
            @Value("${spring.activemq.user}") String user,
            @Value("${spring.activemq.password}") String password
    ) {
        ActiveMQConnectionFactory connectionFactory =
                new ActiveMQConnectionFactory(user, password, brokerUrl);

        RedeliveryPolicy redeliveryPolicy = connectionFactory.getRedeliveryPolicy();

        redeliveryPolicy.setInitialRedeliveryDelay(1000);
        redeliveryPolicy.setRedeliveryDelay(2000);
        redeliveryPolicy.setUseExponentialBackOff(true);
        redeliveryPolicy.setBackOffMultiplier(2.0);
        redeliveryPolicy.setMaximumRedeliveries(3);

        return connectionFactory;
    }
}