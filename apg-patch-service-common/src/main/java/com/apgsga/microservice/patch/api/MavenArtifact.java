package com.apgsga.microservice.patch.api;

import com.affichage.persistence.common.client.EntityRootInterface;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@EntityRootInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
public interface MavenArtifact {

	public static final String ARTIFACT_ID = "artifactId";

	public static final String GROUP_ID = "groupId";

	public static final String NAME = "name";

	public static final String VERSION = "version";
	
	public String getArtifactId();

	public void setArtifactId(String artefactid);

	public String getGroupId();

	public void setGroupId(String groupId);

	public String getVersion();

	public void setVersion(String version);

	public String getName();

	public void setName(String name);
	
	public boolean hasConflict();
	
	public void setHasConflict(boolean hasConflict);

	public Integer getDependencyLevel(); 
	
	public void augmentDependencyLevel();

}
