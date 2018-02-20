package com.apgsga.dm.gradle;

import static org.junit.Assert.*;

import java.io.File;

import org.gradle.api.Project; 
import org.gradle.testfixtures.ProjectBuilder; 
import org.junit.*; 


public class VersionPropertyPluginTests {

	@Before
	public void preconditions() {
		File versionsFile = new File(VersionProperties.VERSIONS_PROPERTY_DEFAULT_PATH + "/versions.properties"); 
		if (versionsFile.exists()) {
			versionsFile.delete(); 
		}
	}
	 
	@Test 
	public void test_defaults() {
		
		Project project = ProjectBuilder.builder().build(); 
		project.getPlugins().apply("com.apgsga.versions-properties"); 
		VersionProperties defaultsVersionProperties = project.getExtensions().findByType(VersionProperties.class);
		assertEquals(defaultsVersionProperties.getLocalMavenRepository(), VersionProperties.LOCAL_REP_DEFAULT);
		assertEquals(defaultsVersionProperties.getMavenVersion(), VersionProperties.VERSION_DEFAULT );
		assertEquals(defaultsVersionProperties.getPropertiesFilePath(), VersionProperties.VERSIONS_PROPERTY_DEFAULT_PATH);
		assertNull(defaultsVersionProperties.getTargetArtefactKey()); 
		File versionsFile = new File(VersionProperties.VERSIONS_PROPERTY_DEFAULT_PATH + "/versions.properties"); 
		assertTrue(versionsFile.exists()); 
	}
}
