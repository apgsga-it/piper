package com.apgsga.microservice.patch.api.impl;

import com.affichage.persistence.common.client.AbstractTransientEntity;
import com.apgsga.microservice.patch.api.MavenArtifact;

public class MavenArtifactBean extends AbstractTransientEntity implements MavenArtifact {

	private static final long serialVersionUID = 1L;

	private String artifactId;
	private String groupId;
	private String name;
	private String version;
	private boolean hasConflict = false;
	
	public MavenArtifactBean() {
		super();
	}

	public MavenArtifactBean(String artifactId, String groupId, String version) {
		super();
		this.artifactId = artifactId;
		this.groupId = groupId;
		this.version = version; 
	}
	
	@Override
	public String getArtifactId() {
		return artifactId;
	}


	@Override
	public void setArtifactId(String artifactId) {
		final Object oldValue = this.artifactId;
		this.artifactId = artifactId;
		firePropertyChangeAndMarkDirty(ARTIFACT_ID, oldValue, artifactId);
	}

	@Override
	public String getGroupId() {
		return groupId;
	}

	@Override
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
		MavenArtifactBean other = (MavenArtifactBean) obj;
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
		return "MavenArtifactImpl [artifactId=" + artifactId + ", groupId=" + groupId + ", name=" + name + ", version="
				+ version + "]";
	}

	@Override
	public boolean hasConflict() {
		return this.hasConflict;
	}

	@Override
	public void setHasConflict(boolean hasConflict) {
		this.hasConflict = hasConflict;
	}
	
	

}
