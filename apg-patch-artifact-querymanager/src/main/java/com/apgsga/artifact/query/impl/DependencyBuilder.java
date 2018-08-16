package com.apgsga.artifact.query.impl;

import java.util.List;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;

import com.apgsga.microservice.patch.api.MavenArtifact;
import com.google.common.collect.Lists;

public class DependencyBuilder implements DependencyVisitor {

	private final MavenArtifact artifactToResolve;
	private final List<MavenArtifact> artifacts;
	private final List<MavenArtifact> dependencies = Lists.newArrayList();

	public DependencyBuilder(MavenArtifact artifactToResolve, List<MavenArtifact> artifacts) {
		super();
		this.artifactToResolve = artifactToResolve;
		this.artifacts = artifacts;
	}

	@Override
	public boolean visitEnter(DependencyNode node) {
		final Artifact artifact = node.getArtifact();
		final MavenArtifact mvnArt = lookupArtifact(artifact);
		if (mvnArt != null) {
			dependencies.add(mvnArt);
		}
		return true;
	}

	private MavenArtifact lookupArtifact(Artifact artifact) {
		if (artifactToResolve.getArtifactId().equals(artifact.getArtifactId())
				&& artifactToResolve.getGroupId().equals(artifact.getGroupId())) {
			return null;
		}
		for (MavenArtifact mvnArt : artifacts) {
			if (mvnArt.getArtifactId().equals(artifact.getArtifactId())
					&& mvnArt.getGroupId().equals(artifact.getGroupId())) {
				return mvnArt;
			}
		}
		return null;
	}

	@Override
	public boolean visitLeave(DependencyNode node) {
		return true;
	}

	public MavenArtWithDependencies create() {

		return new MavenArtWithDependencies(artifactToResolve, dependencies);
	}

}
