package com.apgsga.artifact.query.impl;

import com.apgsga.microservice.patch.api.MavenArtifact;

import java.util.List;

public class MavenArtWithDependencies {

	private final MavenArtifact artifact;
	private List<MavenArtifact> dependencies;

	public MavenArtWithDependencies(MavenArtifact artifact, List<MavenArtifact> dependencies) {
		super();
		this.artifact = artifact;
		this.dependencies = dependencies;
	}

	public MavenArtifact getArtifact() {
		return artifact;
	}

	public List<MavenArtifact> getDependencies() {
		return dependencies;
	}

	public void setDependencies(List<MavenArtifact> dependencies) {
		this.dependencies = dependencies;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((artifact == null) ? 0 : artifact.hashCode());
		result = prime * result + ((dependencies == null) ? 0 : dependencies.hashCode());
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
		MavenArtWithDependencies other = (MavenArtWithDependencies) obj;
		if (artifact == null) {
			if (other.artifact != null)
				return false;
		} else if (!artifact.equals(other.artifact)) {
			return false;
		}
		if (dependencies == null) {
			return other.dependencies == null;
		} else return dependencies.equals(other.dependencies);
	}

	@Override
	public String toString() {
		return "MavenArtWithDependencies [artifact=" + artifact + ", dependencies=" + dependencies + "]";
	}


}
