package com.ruffin.SPRING_BATCH_V2.config;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.h2.server.web.WebServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Configuration de l'application Web avec les API Servlet 3.0.
 */
@Configuration
public class WebConfiguration implements ServletContextInitializer {

    private final Logger log = LoggerFactory.getLogger(WebConfiguration.class);

    private final Environment env;

    public WebConfiguration(Environment env) {
        this.env = env;
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        if (env.getActiveProfiles().length != 0) {
            log.info("La configuration de l'application Web utilise le profile: {}", (Object[]) env.getActiveProfiles());
        }
        log.info("Application Web est entièrement configurée");
    }

	@Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ServletRegistrationBean h2servletRegistration(){
		ServletRegistrationBean registrationBean = new ServletRegistrationBean(new WebServlet());
        registrationBean.addUrlMappings("/console/*");
        return registrationBean;
    }    
}
