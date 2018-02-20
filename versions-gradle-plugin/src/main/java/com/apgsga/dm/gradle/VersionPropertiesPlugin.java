package com.apgsga.dm.gradle;

import java.io.File;
import java.io.IOException;

import org.gradle.api.GradleScriptException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class VersionPropertiesPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.getExtensions().create("versionProperties", VersionProperties.class);
		project.getTasks().create("generateVersionProperties", GenerateVersionProperties.class);
		project.getTasks().create("updateVersionProperties", UpdateVersionsProperties.class);
		VersionProperties parameters = project.getExtensions().findByType(VersionProperties.class);
		File parentDir = new File(parameters.getPropertiesFilePath());
		System.out.println("parentDir: " + parentDir.getAbsolutePath().toString());
		if (!parentDir.exists()) {
			System.out.println("Createing: " + parentDir.toString());
			parentDir.mkdir();
			System.out.println("Created: " + parentDir.toString());
		}
		File versionPropertiesFile = new File(parentDir, "versions.properties");
		if (!versionPropertiesFile.exists()) {
			try {
				System.out.println("Creating: " + versionPropertiesFile.toString());
				versionPropertiesFile.createNewFile();
				System.out.println("Created: " + versionPropertiesFile.toString());
			} catch (IOException e) {
				throw new GradleScriptException("For config: " + parameters.toString(), e);
			}
		}
	}

}
