package vn.plusplus.lms.config.datasource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "lmsEntityManager",
        transactionManagerRef = "lmsTransactionManager",
        basePackages = "vn.plusplus.lms.repository"
)
public class LmsDataSource {

    @Value("${spring.datasource.username}")
    String username;

    @Value("${spring.datasource.password}")
    String password;

    @Value("${spring.datasource.url}")
    String url;

    @Bean
    @Primary
    @ConfigurationProperties(prefix="spring.datasource")
    public DataSource primaryDataSource() {
        return DataSourceBuilder
                .create()
                .username(username)
                .password(password)
                .url(url)
                .build();
    }

    @Primary
    @Bean(name = "lmsEntityManager")
    public LocalContainerEntityManagerFactoryBean lmsEntityManager(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(primaryDataSource())
                .packages("vn.plusplus.lms.repository.entities")
                .persistenceUnit("lmsPU")
                .build();
    }

    @Bean(name = "lmsTransactionManager")
    public PlatformTransactionManager lmsTransactionManager(@Qualifier("lmsEntityManager") EntityManagerFactory lmsEntityManager) {
        return new JpaTransactionManager(lmsEntityManager);
    }
}