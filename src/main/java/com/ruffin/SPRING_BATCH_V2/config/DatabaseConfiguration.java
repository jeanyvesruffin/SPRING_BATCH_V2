package com.ruffin.SPRING_BATCH_V2.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import liquibase.integration.spring.SpringLiquibase;

/**
 * 
 * Configurations de base de données pour l'application Spring Batch.
 *
 */
@Configuration
@EnableJpaRepositories(value = "com.ruffin.SPRING_BATCH_V2", entityManagerFactoryRef = "batchEntityManagerFactory")
@EnableTransactionManagement
public class DatabaseConfiguration {

	private final Logger log = LoggerFactory.getLogger(DatabaseConfiguration.class);

	private final Environment environment;

	public DatabaseConfiguration(Environment environment) {
		this.environment = environment;
	}
	
	@Bean(name = "batchDataSource")
	public DataSource batchDataSource() {
		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setJdbcUrl(environment.getRequiredProperty("spring.datasource.url"));
		hikariConfig.setUsername(environment.getProperty("spring.datasource.username"));
		hikariConfig.setPassword(environment.getProperty("spring.datasource.password"));
		hikariConfig.setMinimumIdle(environment.getProperty("spring.datasource.min-idle", Integer.class, 2));
		hikariConfig.setMaximumPoolSize(environment.getProperty("spring.datasource.max-active", Integer.class, 100));
		hikariConfig.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
		hikariConfig.setRegisterMbeans(true);
		return new HikariDataSource(hikariConfig);
	}

	@Bean(name="batchJpaVendorAdapter")
	public JpaVendorAdapter batchJpaVendorAdapter() {
		return new HibernateJpaVendorAdapter();
	}

	@Bean(name = "batchEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean batchEntityManagerFactory() {
		LocalContainerEntityManagerFactoryBean emfBean = new LocalContainerEntityManagerFactoryBean();
		emfBean.setDataSource(batchDataSource());
		emfBean.setPackagesToScan("com.ruffin.SPRING_BATCH_V2");
		emfBean.setBeanName("batchEntityManagerFactory");
		emfBean.setJpaVendorAdapter(batchJpaVendorAdapter());

		Properties jpaProperties = new Properties();
		jpaProperties.put("hibernate.physical_naming_strategy",
				environment.getProperty("spring.jpa.hibernate.naming.physical-strategy"));
		jpaProperties.put("hibernate.hbm2ddl.auto", environment.getProperty("spring.jpa.hibernate.ddl-auto", "none"));
		jpaProperties.put("hibernate.jdbc.fetch_size",
				environment.getProperty("spring.jpa.properties.hibernate.jdbc.fetch_size", "200"));
		Integer batchSize = environment.getProperty("spring.jpa.properties.hibernate.jdbc.batch_size", Integer.class,
				100);
		if (batchSize > 0) {
			jpaProperties.put("hibernate.jdbc.batch_size", batchSize);
			jpaProperties.put("hibernate.order_inserts", "true");
			jpaProperties.put("hibernate.order_updates", "true");
		}
		jpaProperties.put("hibernate.show_sql",
				environment.getProperty("spring.jpa.properties.hibernate.show_sql", "false"));
		jpaProperties.put("hibernate.format_sql",
				environment.getProperty("spring.jpa.properties.hibernate.format_sql", "false"));
		emfBean.setJpaProperties(jpaProperties);
		return emfBean;

	}

	@Bean(name="batchTransactionManager")
	public PlatformTransactionManager transactionManager() {
		return new JpaTransactionManager(batchEntityManagerFactory().getObject());
	}

    @Bean
    public MBeanExporter exporter() {
        final MBeanExporter exporter = new MBeanExporter();
        exporter.setExcludedBeans("batchDataSource");
        return exporter;
    }


	@Bean
	public SpringLiquibase liquibase(LiquibaseProperties liquibaseProperties) {
		SpringLiquibase liquibase = new SpringLiquibase();
		liquibase.setDataSource(batchDataSource());
		liquibase.setChangeLog("classpath:config/liquibase/master.xml");
		liquibase.setContexts(liquibaseProperties.getContexts());
		liquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());
		liquibase.setDropFirst(liquibaseProperties.isDropFirst());
		if (environment.acceptsProfiles(Constants.PROFIL_NO_LIQUIBASE_SPRING)) {
			liquibase.setShouldRun(false);
		} else {
			liquibase.setShouldRun(liquibaseProperties.isEnabled());
			log.debug("Configuration de Liquibase");
		}
		return liquibase;
	}
}
