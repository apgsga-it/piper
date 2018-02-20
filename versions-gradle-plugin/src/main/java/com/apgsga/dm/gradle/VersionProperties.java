package com.apgsga.dm.gradle;

public class VersionProperties {
	public static final String LOCAL_REP_DEFAULT = "D:/apg/maven/repository";
	public static final String VERSION_DEFAULT = "9.0.6.ADMIN-UIMIG-SNAPSHOT";
	public static final String VERSIONS_PROPERTY_DEFAULT_PATH = "build";
	private String propertiesFilePath = VERSIONS_PROPERTY_DEFAULT_PATH; 
	private String mavenVersion = VERSION_DEFAULT; 
	private String localMavenRepository = LOCAL_REP_DEFAULT;
	private String targetArtefactKey = null; 
	public String getPropertiesFilePath() { 
		return propertiesFilePath;
	}
	public void setPropertiesFilePath(String propertiesFilePath) {
		this.propertiesFilePath = propertiesFilePath;
	}
	public String getMavenVersion() {
		return mavenVersion;
	}
	public void setMavenVersion(String mavenVersion) {
		this.mavenVersion = mavenVersion;
	}
	public String getLocalMavenRepository() {
		return localMavenRepository;
	}
	public void setLocalMavenRepository(String localMavenRepository) {
		this.localMavenRepository = localMavenRepository;
	}
	public String getTargetArtefactKey() {
		return targetArtefactKey;
	}
	public void setTargetArtefactKey(String targetArtefactKey) {
		this.targetArtefactKey = targetArtefactKey;
	}
	@Override
	public String toString() {
		return "VersionProperties [propertiesFilePath=" + propertiesFilePath + ", mavenVersion=" + mavenVersion
				+ ", localMavenRepository=" + localMavenRepository + ", targetArtefactKey=" + targetArtefactKey + "]";
	}


	
	

}
