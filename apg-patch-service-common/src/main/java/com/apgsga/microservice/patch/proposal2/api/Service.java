package com.apgsga.microservice.patch.proposal2.api;

import java.util.List;

import com.affichage.persistence.common.client.EntityRootInterface;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@EntityRootInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
public interface Service {
	
	public static final String SERVICE_META_DATA = "serviceMetaData";
	public static final String SERVICE_NAME = "serviceName";
	public static final String MAVEN_ARTEFACTS = "mavenArtifacts";


	public void setServiceMetaData(ServiceMetaData serviceData);
	
	public ServiceMetaData getServiceMetaData();
	
	List<MavenArtifact> getMavenArtifacts();

	List<String> getMavenArtifactsAsVcsPath();
	
	List<MavenArtifact> getMavenArtifactsToBuild();

	void setMavenArtifacts(List<MavenArtifact> mavenArtifacts);

	void removeMavenArtifacts(MavenArtifact mavenArtifact);

	void addMavenArtifacts(MavenArtifact mavenArtifact);

}
