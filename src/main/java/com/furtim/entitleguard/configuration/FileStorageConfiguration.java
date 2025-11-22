package com.furtim.entitleguard.configuration;

import java.time.LocalDate;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file")
public class FileStorageConfiguration {

	private String uploadDir;

	LocalDate currentDate = LocalDate.now();

	public String getUploadDir() {
		return uploadDir;
	}

	public void setUploadDir(String uploadDir) {
		this.uploadDir = uploadDir + "/" + currentDate.getYear() + "/" + currentDate.getMonth() + "/"
				+ currentDate.getDayOfMonth();

	}

}
