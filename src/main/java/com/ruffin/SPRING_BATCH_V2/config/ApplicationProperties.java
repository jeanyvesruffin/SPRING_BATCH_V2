package com.ruffin.SPRING_BATCH_V2.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 
 * Proprietes specifiques du Client Batch Loader
 * 
 * Les proprietets sont configures dans les fichiers application.yml.
 *
 */

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