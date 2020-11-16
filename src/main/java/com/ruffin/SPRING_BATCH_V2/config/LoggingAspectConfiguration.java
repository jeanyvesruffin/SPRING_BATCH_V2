package com.ruffin.SPRING_BATCH_V2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;

import com.ruffin.SPRING_BATCH_V2.aop.logging.LoggingAspect;

/**
 * Activation de l'aspect journalisation. Si vous l'utilisez finalement pour la
 * production, il est recommandé d'annoter la classe pour le profil Spring "dev"
 * uniquement afin que l'aspect journalisation ne soit pas appliqué en mode
 * production.
 */

@Configuration
@EnableAspectJAutoProxy
public class LoggingAspectConfiguration {

	@Bean
	public LoggingAspect loggingAspect(Environment env) {
		return new LoggingAspect(env);
	}
}
