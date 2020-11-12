package com.ruffin.SPRING_BATCH_V2;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import com.ruffin.SPRING_BATCH_V2.config.DefaultProfileUtil;



/**
 * 
 * Il s'agit d'une classe Java d'assistance qui fournit une alternative à la
 * création d'un fichier web.xml. Cela ne sera appelé que lorsque l'application
 * est déployée sur un conteneur de servlet comme Tomcat, JBoss, etc.
 *
 */
public class ApplicationWebXML extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {

		DefaultProfileUtil.addDefaultProfile(application.application());
		return application.sources(ClientBatchLoaderApp.class);
	}
}
