package ua.ivan.epam.gym.application.config;

import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan(basePackages = "ua.ivan.epam.gym.application")
@PropertySource("classpath:application.properties")
@Import({
        StorageConfig.class,
        IdGeneratorConfig.class
})
public class AppConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}