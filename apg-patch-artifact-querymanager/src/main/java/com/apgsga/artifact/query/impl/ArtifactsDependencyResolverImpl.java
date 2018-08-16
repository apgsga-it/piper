package com.apgsga.artifact.query.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.google.common.collect.Lists;

public class ArtifactsDependencyResolverImpl implements ArtifactDependencyResolver {

	protected static final Logger LOGGER = LoggerFactory.getLogger(ArtifactsDependencyResolverImpl.class);
	
	private static final PatternInclusionsDependencyFilter filter1 = new PatternInclusionsDependencyFilter("com.apgsga.*");
	private static final PatternInclusionsDependencyFilter filter2 = new PatternInclusionsDependencyFilter("com.affichage.*");
	private static final OrDependencyFilter DEPENDENCYFIlTER = new OrDependencyFilter(filter1, filter2);

	private final RepositorySystem system;

	private final RepositorySystemSession session;

	private final List<RemoteRepository> repos;

	private void init(String localRepo) {
		final ResourceLoader rl = new FileSystemResourceLoader();
		Resource resource = rl.getResource(localRepo);
		if (!resource.exists()) {
			try {
				resource.getFile().mkdir();
			} catch (IOException e) {
				throw ExceptionFactory.createPatchServiceRuntimeException("ArtifactsDependencyResolverImpl.init.exception",
						new Object[] { e.getMessage() }, e);			}
		}
	}

	public ArtifactsDependencyResolverImpl(String localRepo) {
		init(localRepo);
		this.system = RepositorySystemFactory.newRepositorySystem();
		this.session = RepositorySystemFactory.newRepositorySystemSession(system, localRepo);
		this.repos = RepositorySystemFactory.newRepositories();

	}
 

	public List<MavenArtWithDependencies> resolveDependenciesInternal(List<MavenArtifact> artifacts) {
		List<MavenArtWithDependencies> resolvedDependencies = Lists.newArrayList();
        ExecutorService executorService = Executors.newFixedThreadPool(artifacts.size());
        List<Callable<MavenArtWithDependencies>> callables = Lists.newArrayList();
        for (MavenArtifact art : artifacts) {
        		Callable<MavenArtWithDependencies> callable = () ->  {
         		return resolveDependencies(art,Collections.unmodifiableList(artifacts));
        		};
        		callables.add(callable);
		}
        try {
			List<Future<MavenArtWithDependencies>> futures = executorService.invokeAll(callables);
			for (Future<MavenArtWithDependencies> future : futures) {
				resolvedDependencies.add(future.get());
			}
		} catch (InterruptedException e) {
			LOGGER.warn("Interrupted!", e);
		    Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("ArtifactsDependencyResolverImpl.resolveDependenciesInternal.exception",
					new Object[] { e.getMessage() }, e);		
		}
		return resolvedDependencies;
	}

	@Override
	public void resolveDependencies(List<MavenArtifact> artifacts) {
		LOGGER.info("Resolving Dependencies");
		List<MavenArtWithDependencies> resolveDependenciesInternal = resolveDependenciesInternal(artifacts); 
		LOGGER.info("Analysing Dependency Level");
		analyseAndSetDependencyLevel(resolveDependenciesInternal); 
	}

	private void analyseAndSetDependencyLevel(List<MavenArtWithDependencies> resolveDependenciesInternal) {
		for (MavenArtWithDependencies resolvedDep : resolveDependenciesInternal) {
			List<MavenArtifact> dependencies = resolvedDep.getDependencies();
			for (MavenArtifact dependency : dependencies) {
				dependency.augmentDependencyLevel();
			}
		}
	}

	private MavenArtWithDependencies resolveDependencies(MavenArtifact artifact, List<MavenArtifact> artifacts) {
		Dependency dependency = new Dependency(new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), "",
				"jar", artifact.getVersion()), "compile");

		CollectRequest collectRequest = new CollectRequest();
		collectRequest.setRoot(dependency);
		collectRequest.setRepositories(repos);
		DependencyRequest dependencyRequest = new DependencyRequest();
		dependencyRequest.setCollectRequest(collectRequest);
		dependencyRequest.setFilter(DEPENDENCYFIlTER);
		try {
			DependencyNode rootNode = system.resolveDependencies(session, dependencyRequest).getRoot();
			DependencyBuilder visitor = new DependencyBuilder(artifact, artifacts);
			rootNode.accept(visitor);
			return visitor.create();			

		} catch (DependencyResolutionException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("ArtifactsDependencyResolverImpl.resolveDependencies.exception",
					new Object[] { e.getMessage() }, e);			
		}

	}
	
	
	

}