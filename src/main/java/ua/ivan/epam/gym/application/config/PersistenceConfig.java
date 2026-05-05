package ua.ivan.epam.gym.application.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@RequiredArgsConstructor
public class PersistenceConfig {
    private final Environment env;

    @Bean
    public DataSource dataSource() {
        var dataSource = new HikariDataSource();

        dataSource.setJdbcUrl(env.getRequiredProperty("db.url"));
        dataSource.setUsername(env.getRequiredProperty("db.username"));
        dataSource.setPassword(env.getRequiredProperty("db.password"));
        dataSource.setDriverClassName(env.getRequiredProperty("db.driver-class-name"));

        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        var factory = new LocalContainerEntityManagerFactoryBean();

        factory.setDataSource(dataSource);
        factory.setPackagesToScan("ua.ivan.epam.gym.application.model");

        var vendorAdapter = new HibernateJpaVendorAdapter();
        factory.setJpaVendorAdapter(vendorAdapter);

        factory.setJpaProperties(hibernateProperties());

        return factory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    private Properties hibernateProperties() {
        Properties properties = new Properties();

        properties.put(
                AvailableSettings.DIALECT,
                env.getRequiredProperty("hibernate.dialect")
        );
        properties.put(
                AvailableSettings.HBM2DDL_AUTO,
                env.getRequiredProperty("hibernate.hbm2ddl.auto")
        );
        properties.put(
                AvailableSettings.SHOW_SQL,
                env.getRequiredProperty("hibernate.show_sql")
        );
        properties.put(
                AvailableSettings.FORMAT_SQL,
                env.getRequiredProperty("hibernate.format_sql")
        );

        return properties;
    }
}
