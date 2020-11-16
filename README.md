# BATCH_V2

# Prerecquis

Java version 15.0.1

# Spring batch ?

Spring Batch permet l'execution de traitement programmme appele job.

Il prend en entree un fichier csv, par exemple.

Puis, execute periodiquement un traitement sur ces donnees.

Pour enfin, ecrire en base de donnees ou declencher d'autre job.

# Exemple de scenarios

**Transformation de donnees**

* Processus comptable de fin de mois
* Processus quotidien de commande au détail des processus
* Journal horaire des transactions

**Integration system**

* Importation en masse de donnees externes
* Importation en masse de donnees entre les systemes internes
* exportation en masse de donnees


# Flux d'execution

* Donnees d'entrees sont structurees en XML ou JSON, proviennent generalements d'une file (Queue) de message.
* Le job, s'execute presque en temps reel pour traiter les messages entrants.
* La sortie est alors l'ecriture de donnees en base de donnee ou alors le declenchement d'un autre job.


## Avantages

* Gerer avec succes de gros volumes de donnees
* Peut arreter et redemarrer le travail a tout moment
* Approche mature

## Inconvenients

* Ensemble de donnees d'entree fini
* Peut avoir un impact sur le traitement en temps reel car tres gourmand en ressources systeme.
* Exagere pour des calculs plus simples

# Initialisation du projet

1. Creation d'un projet Maven

2. Ajout d'un fichier Readme et gitignore

3. Initialisation depot git

```cmd
cd [projet]
git init
git add .
git commit -am "init projet"
```

4. Creation d'un repository dans github

5. Faire le lien entre le repository local et Github

```cmd
git branch -M main
git remote add origin https://github.com/jeanyvesruffin/SPRING_BATCH_V2.git
git push -u origin main
```

# Creation du projet Maven, pom.XML et choix des dependences utilisees

Nous utiliserons les dependences suivantes dans notre projet:
* **Jackson**: permettent de propose plusieurs approches pour travailler avec JSON, y compris l'utilisation d'annotations de liaison sur les classes POJO pour des cas d'utilisation simples.
* **Hibernate**: est un framework open source gerant la persistance des objets en base de donnees relationnelle.
* **Spring**: framework java.
* **HikariCP**: est un pool de connexions est un cache des connexions de base de donnees, maintenu afin que les connexions puissent etre reutilisees lorsque de futures demandes a la base de donnees sont requises
* **Commons-lang3**: fournit une foule d'utilitaires d'aide pour l'API java.lang, notamment les methodes de manipulation de chaines, les methodes numeriques de base, la reflexion d'objets, la concurrence, la creation et la serialisation et les proprietes systeme. En outre, il contient des ameliorations de base de java.util.Date et une serie d'utilitaires dedies a l'aide a la creation	de methodes, telles que hashCode, toString et equals.
* **Commons-io**: est une bibliotheque d'utilitaires pour aider au developpement de la fonctionnalite IO.
* **Javax.transaction-api**: L'API de transaction Java (JTA) specifie les interfaces Java standard entre un gestionnaire de transactions et les parties impliquees dans un systeme de transaction distribue: le gestionnaire de ressources, le serveur d'applications et les applications transactionnelles.
* **Liquibase**: est une solution open-source de gestion des changements de schema de base de donnees qui vous permet de gerer facilement les revisions de vos changements de base de donnees.
* **Assertj**: est une bibliotheque java fournissant un riche ensemble d'assertions, des messages d'erreur vraiment utiles, ameliore la lisibilite du code de test et est conçue pour etre tres facile a utiliser dans votre IDE prefere.
* **Junit-jupiter-api**: Contrairement aux versions precedentes de JUnit, JUnit 5 est compose de plusieurs modules differents issus de trois sous-projets differents.JUnit 5 = Plateforme JUnit + JUnit Jupiter + JUnit Vintage
* **Mockito**: permet la realisation de tests unitaire a l'aide de mock (objet simule)
* **hamcrest**: permet la realisation de tests unitaire a l'aide de matcher. C'est une bibliotheque de correspondance, qui peut etre combinee pour creer des expressions d'intention flexibles dans les tests.

Avec les versions suivantes:

	<properties>
		<jackson.version>2.12.0-rc1</jackson.version>
		<hibernate.version>6.0.0.Alpha6</hibernate.version>
		<hibernate.type>pom</hibernate.type>
		<spring.version>2.3.5.RELEASE</spring.version>
	</properties>

# Definition du projet

**Les besoins**: 

* creation application spring batch
* utilise un fichier .csv en entre
* alimente une base de donnees MySQL
* comporte des tests unitaires 

**SPRING BATCH**

![SPRING BATCH](https://spring.io/images/diagram-batch-5001274a87227c34b690542c45ca0c9d.svg)

Lors de la sequence d'execution du batch:

* la lecture de l'input sera sur un fichier csv contenant un certain nombre de champs, d'ont un champs "pays".
* Lors de la phase processing, une fonction permettra la creation de deux fichiers csv, representant pour l'un, les clients habitants en France et pour l'autre les cllietns habitant a l'etrange.


# Configuration du projet

## Configuration environnement spring

### ClientBatchLoaderApp.java

Cette classe sera le point d'entre de l'application Spring Boot est sera annote:

```java
@SpringBootApplication
@EnableConfigurationProperties({LiquibaseProperties.class, ApplicationConfigurationProperties.class})
public class ClientBatchLoaderApp{

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(ClientBatchLoaderApp.class);
	}
}

```

### Ajout de la configuration a l'aide du gestionnaire d'environnement Spring

1 . Ajout attribut environnement membre de la classe spring Environnement
2 . Creation constructeur avec le parametre environnement 

```java
@SpringBootApplication
@EnableConfigurationProperties({LiquibaseProperties.class, ApplicationConfigurationProperties.class})
public class ClientBatchLoaderApp{
	
	private Environment environment; 
	
	public ClientBatchLoaderApp(Environment environment) {
		super();
		this.environment = environment;
	}
	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(ClientBatchLoaderApp.class);
		Environment environment =app.run(args).getEnvironment();
				
	}

}

```

3 . Definition du fichier de configuration necessaire au demarrage de l'application Spring Boot


```java
package config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="application", ignoreUnknownFields = false)
public class ApplicationProperties {

}
```

4 . Creation des fichiers de configuration d'environnement (fichier .yml) et bugFix H2


*/batch_v2/src/main/resources/config/application.yml*


```yml

spring:
    application:
        name: ClientBatchLoader
    profiles:Dspring.profiles.active` set in `JAVA_OPTS`
        active: #spring.profiles.active#
    jackson:
        serialization.write_dates_as_timestamps: false
    jpa:
        open-in-view: false
        hibernate:
            ddl-auto: none
            naming:
                physical-strategy: org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy
                implicit-strategy: org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy

info:
    project:
        version: #project.version#

management:
  endpoints:
    enabled-by-default: true
    web:
      exposure:
        include: '*'

```


*/batch_v2/src/main/resources/config/application-dev.yml*

```yml

spring:
    profiles:
        active: dev
    devtools:
        restart:
            enabled: true
    jackson:
        serialization.indent_output: true
    datasource:
        type: com.zaxxer.hikari.HikariDataSource
        url: jdbc:h2:file:./h2db/db/clientbatchloader-dev;
        username: ClientBatchLoader
        password:
    h2:
        console:
            enabled: true
    jpa:
        database-platform: com.pluralsight.springbatch.patientbatchloader.config.FixedH2Dialect
        database: H2
        show-sql: true
        properties:
            hibernate.id.new_generator_mappings: true
            hibernate.cache.use_second_level_cache: false
            hibernate.cache.use_query_cache: false
            hibernate.generate_statistics: true
    liquibase:
        contexts: dev
        drop-first: true

server:
    port: 8080

```


*/batch_v2/src/main/resources/config/application-prod.yml*

```yml
logging:
    level:
        ROOT: INFO
        clientBatchLoader: INFO

server:
    port: 8080
    compression:
        enabled: true
        mime-types: text/html,text/xml,text/plain,text/css, application/javascript, application/json
        min-response-size: 1024

spring:
    devtools:
        restart:
            enabled: false
    datasource:
        type: com.zaxxer.hikari.HikariDataSource
        url: jdbc:h2:file:./h2db/db/clientbatchloader-prod;
        username: ClientBatchLoader
        password:
    h2:
        console:
            enabled: true
    jpa:
        database-platform: clientbatchloader.config.FixedH2Dialect
        database: H2
        show-sql: true
        properties:
            hibernate.id.new_generator_mappings: true
            hibernate.cache.use_second_level_cache: false
            hibernate.cache.use_query_cache: false
            hibernate.generate_statistics: true
    liquibase:
        contexts: prod

```

*/batch_v2/src/main/java/config/FixedH2Dialect.java*

```java
package config;

import java.sql.Types;

import org.hibernate.dialect.H2Dialect;

/**
 * Resout un probleme avec les types de colonnes dans H2, en particulier autour des nouvelles API de temps. 
 */
public class FixedH2Dialect extends H2Dialect{
	
	public FixedH2Dialect() {
		super();
		registerColumnType(Types.FLOAT, "real");
		registerColumnType(Types.BINARY, "varbinary");
	}

}
```

5 . Creation de la methode de selection de l'environnement à l'aide de la classes de constante

*/batch_v2/src/main/java/config/Constants.java*

```java
package config;


/**
 * 
 * Constantes de l'application
 *
 */
public class Constants {

	public static final String COMPTE_SYSTEME = "system";
	public static final String CLE_DEFAULT_LANG = "fr";
	public static final String PROFIL_DEVELOPPEMENT_SPRING = "dev";
	public static final String PROFIL_PRODUCTION_SPRING = "prod";
	public static final String PROFIL_NO_LIQUIBASE_SPRING = "no-liquibase";
	
	private Constants() {
		
	}
	
}
```


6 . Creation du fichier de configuration par default, initilisation de l'application et choix de l'environnement.

*/batch_v2/src/main/java/config/DefaultProfileUtil.java*

```java
package config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.core.env.Environment;

/**
 * 
 * Classe utilitaire pour charger un profil Spring à utiliser par défaut
 * lorsqu'il n'y a pas de spring.profiles.active défini dans l'environnement ou
 * pas d'argument de ligne de commande. Si la valeur n'est pas disponible dans
 * application.yml, le profil dev sera utilisé par défaut.
 *
 */
public class DefaultProfileUtil {

	private static final String PROFIL_DEFAULT_SPRING = "spring.profiles.default";

	private DefaultProfileUtil() {

	}

	public static void addDefaultProfile(SpringApplication app) {
		Map<String, Object> defaultProperties = new HashMap<String, Object>();
		defaultProperties.put(PROFIL_DEFAULT_SPRING, Constants.PROFIL_DEVELOPPEMENT_SPRING);
		app.setDefaultProperties(defaultProperties);
	}

	public static String[] getActiveProfiles(Environment environment) {
		String[] profiles = environment.getActiveProfiles();
		if (profiles.length == 0) {
			return environment.getDefaultProfiles();
		}
		return profiles;
	}

}
```


*/batch_v2/src/main/java/clientBatchLoader/ClientBatchLoaderApp.java* 
 
```java
@PostConstruct
public void initApplication() {
	Collection<String> profilActif = Arrays.asList(environment.getActiveProfiles());
	if (profilActif.contains(Constants.PROFIL_DEVELOPPEMENT_SPRING)  && profilActif.contains(Constants.PROFIL_PRODUCTION_SPRING)) {
		log.error("Vous avez mal configuré votre application! Il ne doit pas fonctionner avec les profils «dev» et «prod» en même temps.");
	}
}
```

7 . Et enfin, allons creer une classe AppllicationWebXML necessaire au demarrage de l'application.

Il s'agit d'une classe Java d'assistance qui fournit une alternative à la création d'un fichier web.xml.
Cela ne sera appelé que lorsque l'application est déployée sur un conteneur de servlet comme Tomcat, JBoss, etc.


*/batch_v2/src/main/java/clientBatchLoader/ApplicationWebXML.java*

```java
public class ApplicationWebXML extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {

		DefaultProfileUtil.addDefaultProfile(application.application());
		return application.sources(ClientBatchLoaderApp.class);
	}
}

```

### Configuration base de donnee H2

*/SPRING_BATCH_V2/src/main/java/com/ruffin/SPRING_BATCH_V2/config/DatabaseConfiguration.java*

```java
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
	private JpaVendorAdapter batchJpaVendorAdapter() {
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

    @SuppressWarnings("deprecation")
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

```

# Creation a Spring Batch application

## Ajouts des dependances Spring Batch

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-batch</artifactId>
	<version>2.4.0</version>
</dependency>
<dependency>
	<groupId>org.springframework.batch</groupId>
	<artifactId>spring-batch-test</artifactId>
	<version>4.3.0</version>
	<scope>test</scope>
</dependency>
```

## Configuration de Spring Batch

Definition de la strategie Spring Batch.

1 . Declaration des attributs membres aux classes Spring Batch

* JobRepository est responsable de la persitance des donneese.
* JobExplorer fournis les getters et setters pour explorer les donnees du JobRepository.
* JobLauncher execute le job avec les parametres donnees.

2 . Cablage (Override) batchTransactionManager et batchDataSource

3 . Definition de l'implementation BatchConfigurer.

4. Creation des methodes createJobLauncher,afterPropertiesSet et createJobRepository

*package com.ruffin.SPRING_BATCH_V2.config;*

```java

@Component
@EnableBatchProcessing
public class BatchConfiguration implements BatchConfigurer {

	private JobRepository jobRepository;
	private JobExplorer jobExplorer;
	private JobLauncher jobLauncher;

	@Autowired
	@Qualifier(value = "batchTransactionManager")
	private PlatformTransactionManager batchTransactionManager;

	@Autowired
	@Qualifier(value = "batchDataSource")
	private DataSource batchDataSource;

	@Override
	public JobRepository getJobRepository() throws Exception {
		return this.jobRepository;
	}

	@Override
	public PlatformTransactionManager getTransactionManager() throws Exception {
		return this.batchTransactionManager;
	}

	@Override
	public JobLauncher getJobLauncher() throws Exception {
		return this.jobLauncher;
	}

	@Override
	public JobExplorer getJobExplorer() throws Exception {
		return this.jobExplorer;
	}

	protected JobLauncher createJobLauncher() throws Exception {
		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
		jobLauncher.setJobRepository(jobRepository);
		jobLauncher.afterPropertiesSet();
		return jobLauncher;
	}

	protected JobRepository createJobRepository() throws Exception {
		JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
		factory.setDataSource(this.batchDataSource);
		factory.setTransactionManager(getTransactionManager());
		factory.afterPropertiesSet();
		return factory.getObject();

	}

	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		this.jobRepository = createJobRepository();
		JobExplorerFactoryBean jobExplorerFactoryBean = new JobExplorerFactoryBean();
		jobExplorerFactoryBean.setDataSource(this.batchDataSource);
		jobExplorerFactoryBean.afterPropertiesSet();
		this.jobExplorer = jobExplorerFactoryBean.getObject();
		this.jobLauncher = createJobLauncher();

	}
}

```

## Desactivation temporaire de spring batch dans le fichier de configuration application.yml

````yml
spring:
    application:
        name: ClientBatchLoader
    batch:
       job:
          enabled: false
```

Configuration de l'inputPath ou se trouvera le fichier input csv a traiter.
Dans applciation.yml

```yml

application:
   batch:
      inputPath: D:\PROGRAMMING\SPRING_BATCH_V2\SPRING_BATCH_V2\src\main\java\com\ruffin\SPRING_BATCH_V2\data

```

Configuration de l'inputPath ou se trouvera le fichier input csv a traiter.
Dans applicationProperties.java

```java

@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

	private final Batch batch = new Batch();

	public Batch getBatch() {
		return batch;
	}

	public static class Batch {
		private String inputPath = "D:/PROGRAMMING/SPRING_BATCH_V2/SPRING_BATCH_V2/src/main/java/com/ruffin/SPRING_BATCH_V2/data";

		public String getInputPath() {
			return this.inputPath;
		}

		public void setInputPath(String inputPath) {
			this.inputPath = inputPath;
		}

	}

}
```

## Ajout d'un schema de base de donnee a spring batch (metadonnees)


/SPRING_BATCH_V2/src/main/resources/config/liquibase contient les definitions de schema utilisees par liquibase pour gerer le schema et les donnees de la base de donnees.

On y ajoute:

````xml
<include file="config/liquibase/changelog/11162020000000_create_spring_batch_objects.xml" relativeToChangelogFile="false"/>

```

Puis, nous creons le schema Spring Batch, correspondant aux parametres d'instances de'un batch

```xml
<?xml version="1.0" encoding="utf-8"?>
<databaseChangeLog
	xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
	xmlns:ext="http://www.liquibase.org/xml/ns/dbchangelog-ext"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.5.xsd
                        http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd">

	
	<property name="now" value="now()" dbms="h2"/>
	<property name="now" value="GETDATE()" dbms="mssql" />

	<changeSet id="01012018000001" author="system">
		<createTable tableName="BATCH_JOB_INSTANCE">
			<column name="JOB_INSTANCE_ID" type="bigint"
				autoIncrement="${autoIncrement}">
				<constraints primaryKey="true" nullable="false" />
			</column>
			<column name="VERSION" type="bigint" />
			<column name="JOB_NAME" type="VARCHAR(100)">
				<constraints nullable="false" />
			</column>
			<column name="JOB_KEY" type="VARCHAR(32)">
				<constraints nullable="false" />
			</column>
		</createTable>

		<createIndex indexName="JOB_INST_UN"
			tableName="BATCH_JOB_INSTANCE" unique="true">
			<column name="JOB_NAME" type="varchar(100)" />
			<column name="JOB_KEY" type="varchar(32)" />
		</createIndex>

		<createTable tableName="BATCH_JOB_EXECUTION">
			<column name="JOB_EXECUTION_ID" type="bigint"
				autoIncrement="${autoIncrement}">
				<constraints primaryKey="true" nullable="false" />
			</column>
			<column name="VERSION" type="bigint" />
			<column name="JOB_INSTANCE_ID" type="bigint">
				<constraints nullable="false" />
			</column>
			<column name="CREATE_TIME" type="timestamp">
				<constraints nullable="false" />
			</column>
			<column name="START_TIME" type="timestamp" defaultValue="null" />
			<column name="END_TIME" type="timestamp" defaultValue="null" />
			<column name="STATUS" type="VARCHAR(10)" />
			<column name="EXIT_CODE" type="VARCHAR(2500)" />
			<column name="EXIT_MESSAGE" type="VARCHAR(2500)" />
			<column name="LAST_UPDATED" type="timestamp" />
			<column name="JOB_CONFIGURATION_LOCATION" type="VARCHAR(2500)" />
		</createTable>

		<addForeignKeyConstraint
			baseColumnNames="JOB_INSTANCE_ID" baseTableName="BATCH_JOB_EXECUTION"
			constraintName="JOB_INST_EXEC_FK"
			referencedColumnNames="JOB_INSTANCE_ID"
			referencedTableName="BATCH_JOB_INSTANCE" />

		<createTable tableName="BATCH_JOB_EXECUTION_PARAMS">
			<column name="JOB_EXECUTION_ID" type="bigint"
				autoIncrement="${autoIncrement}">
				<constraints primaryKey="true" nullable="false" />
			</column>
			<column name="TYPE_CD" type="VARCHAR(6)">
				<constraints nullable="false" />
			</column>
			<column name="KEY_NAME" type="VARCHAR(100)">
				<constraints nullable="false" />
			</column>
			<column name="STRING_VAL" type="VARCHAR(250)" />
			<column name="DATE_VAL" type="timestamp" defaultValue="null" />
			<column name="LONG_VAL" type="bigint" />
			<column name="DOUBLE_VAL" type="double precision" />
			<column name="IDENTIFYING" type="CHAR(1)">
				<constraints nullable="false" />
			</column>
		</createTable>

		<addForeignKeyConstraint
			baseColumnNames="JOB_EXECUTION_ID"
			baseTableName="BATCH_JOB_EXECUTION_PARAMS"
			constraintName="JOB_EXEC_PARAMS_FK"
			referencedColumnNames="JOB_EXECUTION_ID"
			referencedTableName="BATCH_JOB_EXECUTION" />

		<createTable tableName="BATCH_STEP_EXECUTION">
			<column name="STEP_EXECUTION_ID" type="bigint"
				autoIncrement="${autoIncrement}">
				<constraints primaryKey="true" nullable="false" />
			</column>
			<column name="VERSION" type="bigint">
				<constraints nullable="false" />
			</column>
			<column name="STEP_NAME" type="varchar(100)">
				<constraints nullable="false" />
			</column>
			<column name="JOB_EXECUTION_ID" type="bigint">
				<constraints nullable="false" />
			</column>
			<column name="START_TIME" type="timestamp">
				<constraints nullable="false" />
			</column>
			<column name="END_TIME" type="timestamp" defaultValue="null" />
			<column name="STATUS" type="varchar(10)" />
			<column name="COMMIT_COUNT" type="bigint" />
			<column name="READ_COUNT" type="bigint" />
			<column name="FILTER_COUNT" type="bigint" />
			<column name="WRITE_COUNT" type="bigint" />
			<column name="READ_SKIP_COUNT" type="bigint" />
			<column name="WRITE_SKIP_COUNT" type="bigint" />
			<column name="PROCESS_SKIP_COUNT" type="bigint" />
			<column name="ROLLBACK_COUNT" type="bigint" />
			<column name="EXIT_CODE" type="varchar(2500)" />
			<column name="EXIT_MESSAGE" type="varchar(2500)" />
			<column name="LAST_UPDATED" type="timestamp" />
		</createTable>

		<addForeignKeyConstraint
			baseColumnNames="JOB_EXECUTION_ID"
			baseTableName="BATCH_STEP_EXECUTION"
			constraintName="JOB_EXEC_STEP_FK"
			referencedColumnNames="JOB_EXECUTION_ID"
			referencedTableName="BATCH_JOB_EXECUTION" />

		<createTable tableName="BATCH_STEP_EXECUTION_CONTEXT">
			<column name="STEP_EXECUTION_ID" type="bigint"
				autoIncrement="${autoIncrement}">
				<constraints primaryKey="true" nullable="false" />
			</column>
			<column name="SHORT_CONTEXT" type="varchar(2500)">
				<constraints nullable="false" />
			</column>
			<column name="SERIALIZED_CONTEXT" type="LONGVARCHAR" />
		</createTable>

		<addForeignKeyConstraint
			baseColumnNames="STEP_EXECUTION_ID"
			baseTableName="BATCH_STEP_EXECUTION_CONTEXT"
			constraintName="STEP_EXEC_CTX_FK"
			referencedColumnNames="STEP_EXECUTION_ID"
			referencedTableName="BATCH_STEP_EXECUTION" />

		<createTable tableName="BATCH_JOB_EXECUTION_CONTEXT">
			<column name="JOB_EXECUTION_ID" type="bigint"
				autoIncrement="${autoIncrement}">
				<constraints primaryKey="true" nullable="false" />
			</column>
			<column name="SHORT_CONTEXT" type="varchar(2500)">
				<constraints nullable="false" />
			</column>
			<column name="SERIALIZED_CONTEXT" type="LONGVARCHAR" />
		</createTable>

		<addForeignKeyConstraint
			baseColumnNames="JOB_EXECUTION_ID"
			baseTableName="BATCH_JOB_EXECUTION_CONTEXT"
			constraintName="JOB_EXEC_CTX_FK"
			referencedColumnNames="JOB_EXECUTION_ID"
			referencedTableName="BATCH_JOB_EXECUTION" />

		<createSequence sequenceName="BATCH_STEP_EXECUTION_SEQ" />
		<createSequence sequenceName="BATCH_JOB_EXECUTION_SEQ" />
		<createSequence sequenceName="BATCH_JOB_SEQ" />

	</changeSet>
</databaseChangeLog>
```




























# Tricks et TIPS

ERROR:

```cmd
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/C:/Users/admin/.p2/pool/plugins/org.eclipse.m2e.maven.runtime.slf4j.simple_1.16.0.20200610-1735/jars/slf4j-simple-1.7.5.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [file:/C:/Users/admin/eclipse/jee-2020-09/eclipse/configuration/org.eclipse.osgi/5/0/.cp/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
SLF4J: Actual binding is of type [org.slf4j.impl.SimpleLoggerFactory]
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/C:/Users/admin/.p2/pool/plugins/org.eclipse.m2e.maven.runtime.slf4j.simple_1.16.0.20200610-1735/jars/slf4j-simple-1.7.5.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [file:/C:/Users/admin/eclipse/jee-2020-09/eclipse/configuration/org.eclipse.osgi/5/0/.cp/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
SLF4J: Actual binding is of type [org.slf4j.impl.SimpleLoggerFactory]
```

Solve:

1 . Rechercher les Jar en conflit:

```cmd
mvn dependency:tree
```

Plan B qui fonctionne car le problemee viens de l'IDE

https://stackoverflow.com/questions/63755390/multiple-slf4j-bindings-with-m2e-in-eclipse-2020-06?noredirect=1#comment112749280_63755390

ERROR Jackson
Solve ajout argument --add-opens java.base/java.lang=ALL-UNNAMED
