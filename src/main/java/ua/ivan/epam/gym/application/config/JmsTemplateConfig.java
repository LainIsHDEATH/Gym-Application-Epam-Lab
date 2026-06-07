package ua.ivan.epam.gym.application.config;

import jakarta.jms.DeliveryMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

@Configuration
public class JmsTemplateConfig {

    @Bean
    public JmsTemplateCustomizer persistentJmsTemplateCustomizer() {
        return jmsTemplate -> {
            jmsTemplate.setDeliveryPersistent(true);
            jmsTemplate.setExplicitQosEnabled(true);
            jmsTemplate.setDeliveryMode(DeliveryMode.PERSISTENT);
        };
    }

    public interface JmsTemplateCustomizer {
        void customize(JmsTemplate jmsTemplate);
    }
}