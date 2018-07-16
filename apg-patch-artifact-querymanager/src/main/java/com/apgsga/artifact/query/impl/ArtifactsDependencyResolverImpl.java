package com.apgsga.artifact.query.impl;

import java.io.IOException;
import java.util.List;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.util.filter.OrDependencyFilter;
import org.eclipse.aether.util.filter.PatternInclusionsDependencyFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.apgsga.artifact.query.ArtifactDependencyResolver;
import com.apgsga.microservice.patch.api.MavenArtifact;
import com.google.common.collect.Lists;

public class ArtifactsDependencyResolverImpl implements ArtifactDependencyResolver {

	protected static final Logger LOGGER = LoggerFactory.getLogger(ArtifactsDependencyResolverImpl.class);

	private final RepositorySystem system;

	private final RepositorySystemSession session;

	private final List<RemoteRepository> repos;

	private void init(String localRepo) {
		final ResourceLoader rl = new FileSystemResourceLoader();
		Resource resource = rl.getResource(localRepo);
		if (!resource.exists()) {
			// TODO (che, 25.1) : Do we want this? Correct here?
			try {
				resource.getFile().mkdir();
			} catch (IOException e) {
				throw new RuntimeException("Local Repository directory could'nt be created", e);
			}
		}
	}

	public ArtifactsDependencyResolverImpl(String localRepo) {
		init(localRepo);
		this.system = RepositorySystemFactory.newRepositorySystem();
		this.session = RepositorySystemFactory.newRepositorySystemSession(system, localRepo);
		this.repos = RepositorySystemFactory.newRepositories(system, session);

	}

	@Override
	public List<MavenArtWithDependencies> resolveDependencies(List<MavenArtifact> artifacts) {
		List<MavenArtWithDependencies> resolvedDep = Lists.newArrayList();
		for (MavenArtifact art : artifacts) {
			resolveDependency(art,artifacts, resolvedDep);
		}
		return resolvedDep;
	}

	private void resolveDependency(MavenArtifact artifact, List<MavenArtifact> artifacts, List<MavenArtWithDependencies> resolvedDep) {
		Dependency dependency = new Dependency(new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), "",
				"jar", artifact.getVersion()), "compile");

		CollectRequest collectRequest = new CollectRequest();
		collectRequest.setRoot(dependency);
		collectRequest.setRepositories(repos);
		PatternInclusionsDependencyFilter filter1 = new PatternInclusionsDependencyFilter("com.apgsga.*");
		PatternInclusionsDependencyFilter filter2 = new PatternInclusionsDependencyFilter("com.affichage.*");
		OrDependencyFilter depFilter = new OrDependencyFilter(filter1, filter2);
		DependencyRequest dependencyRequest = new DependencyRequest();
		dependencyRequest.setCollectRequest(collectRequest);
		dependencyRequest.setFilter(depFilter);
		try {
			DependencyNode rootNode = system.resolveDependencies(session, dependencyRequest).getRoot();
			DependencyBuilder visitor = new DependencyBuilder(artifact, artifacts);
			rootNode.accept(visitor);
			resolvedDep.add(visitor.create());			

		} catch (DependencyResolutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
