package com.ruffin.SPRING_BATCH_V2.aop.logging;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import com.ruffin.SPRING_BATCH_V2.config.Constants;

/**
 * Aspect pour la journalisation de l'exécution des composants du service et du
 * référentiel Spring.
 */
@Aspect
public class LoggingAspect {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final Environment env;

	public LoggingAspect(Environment env) {
		this.env = env;
	}

	/**
	 * Pointcut qui correspond à tous les référentiels, services et points de
	 * terminaison Web REST.
	 */
	@Pointcut("within(@org.springframework.stereotype.Service *)"
			+ " || within(@org.springframework.web.bind.annotation.RestController *)")
	public void springBeanPointcut() {
		// La méthode est vide car il ne s'agit que d'un Pointcut, les implémentations
		// sont dans les advices.
	}

	/**
	 * Pointcut qui correspond à tous les beans Spring dans les packages principaux
	 * de l'application.
	 */
	@Pointcut("within(com.pluralsight.springbatch.patientbatchloader.service..*)"
			+ " || within(com.pluralsight.springbatch.patientbatchloader.web.rest..*)")
	public void applicationPackagePointcut() {
		// La méthode est vide car il ne s'agit que d'un Pointcut, les implémentations
		// sont dans les advices.
	}

	/**
	 * Advices qui enregistre les méthodes lançant des exceptions.
	 *
	 * @param joinPoint join point for advice
	 * @param e         exception
	 */
	@SuppressWarnings("deprecation")
	@AfterThrowing(pointcut = "applicationPackagePointcut() && springBeanPointcut()", throwing = "e")
	public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
		if (env.acceptsProfiles(Constants.PROFIL_DEVELOPPEMENT_SPRING)) {
			log.error("Exception in {}.{}() with cause = \'{}\' and exception = \'{}\'",
					joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName(),
					e.getCause() != null ? e.getCause() : "NULL", e.getMessage(), e);

		} else {
			log.error("Exception in {}.{}() with cause = {}", joinPoint.getSignature().getDeclaringTypeName(),
					joinPoint.getSignature().getName(), e.getCause() != null ? e.getCause() : "NULL");
		}
	}

	/**
	 * Advice qui enregistre lorsqu'une méthode est entrée et sortie.
	 *
	 * @param joinPoint join point for advice
	 * @return result
	 * @throws Throwable throws IllegalArgumentException
	 */
	@Around("applicationPackagePointcut() && springBeanPointcut()")
	public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
		if (log.isDebugEnabled()) {
			log.debug("Enter: {}.{}() with argument[s] = {}", joinPoint.getSignature().getDeclaringTypeName(),
					joinPoint.getSignature().getName(), Arrays.toString(joinPoint.getArgs()));
		}
		try {
			Object result = joinPoint.proceed();
			if (log.isDebugEnabled()) {
				log.debug("Exit: {}.{}() with result = {}", joinPoint.getSignature().getDeclaringTypeName(),
						joinPoint.getSignature().getName(), result);
			}
			return result;
		} catch (IllegalArgumentException e) {
			log.error("Illegal argument: {} in {}.{}()", Arrays.toString(joinPoint.getArgs()),
					joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());

			throw e;
		}
	}
}
