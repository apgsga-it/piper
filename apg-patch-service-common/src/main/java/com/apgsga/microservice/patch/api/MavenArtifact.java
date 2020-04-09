package com.apgsga.microservice.patch.api;

import com.affichage.persistence.common.client.AbstractTransientEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class MavenArtifact extends AbstractTransientEntity  {

	private static final long serialVersionUID = 1L;
	public static final String ARTIFACT_ID = "artifactId";

	public static final String GROUP_ID = "groupId";

	public static final String NAME = "name";

	public static final String VERSION = "version";

	private String artifactId;
	private String groupId;
	private String name;
	private String version;
	@JsonIgnore
	private transient boolean hasConflict = false;
	private Integer dependencyLevel = 0; 
	
	public MavenArtifact() {
		super();
	}

	public MavenArtifact(String artifactId, String groupId, String version) {
		super();
		this.artifactId = artifactId;
		this.groupId = groupId;
		this.version = version; 
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		final Object oldValue = this.artifactId;
		this.artifactId = artifactId;
		firePropertyChangeAndMarkDirty(ARTIFACT_ID, oldValue, artifactId);
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		final Object oldValue = this.groupId;
		this.groupId = groupId;
		firePropertyChangeAndMarkDirty(ARTIFACT_ID, oldValue, groupId);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		final Object oldValue = this.name;
		this.name = name;
		firePropertyChangeAndMarkDirty(NAME, oldValue, name);
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		final Object oldValue = this.version;
		this.version = version;
		firePropertyChangeAndMarkDirty(VERSION, oldValue, version);
	}
	

	public Integer getDependencyLevel() {
		return dependencyLevel;
	}

	public void augmentDependencyLevel() {
		dependencyLevel  = dependencyLevel + 1;
	}

	public boolean hasConflict() {
		return this.hasConflict;
	}

	public void setHasConflict(boolean hasConflict) {
		this.hasConflict = hasConflict;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((artifactId == null) ? 0 : artifactId.hashCode());
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MavenArtifact other = (MavenArtifact) obj;
		if (artifactId == null) {
			if (other.artifactId != null)
				return false;
		} else if (!artifactId.equals(other.artifactId))
			return false;
		if (groupId == null) {
			if (other.groupId != null)
				return false;
		} else if (!groupId.equals(other.groupId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MavenArtifactBean [artifactId=" + artifactId + ", groupId=" + groupId + ", name=" + name + ", version="
				+ version + ", hasConflict=" + hasConflict + ", dependencyLevel=" + dependencyLevel + "]";
	}

	
	

}
