package com.apgsga.artifact.query;

import java.util.List;

import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.repository.RemoteRepository;

import com.apgsga.artifact.query.impl.RepositorySystemFactoryImpl;

public interface RepositorySystemFactory {
	
	public static RepositorySystemFactory create(String baseUrl, String repoName, String user) {
		RepositorySystemFactoryImpl systemFactory = new RepositorySystemFactoryImpl();
		systemFactory.setMavenRepoBaseUrl(baseUrl);
		systemFactory.setMavenRepoUsername(user);
		systemFactory.setMavenRepoName(repoName);
		return systemFactory;
	}
	
	public List<RemoteRepository> newRepositories();
	
	public RepositorySystem newRepositorySystem();
	
	public DefaultRepositorySystemSession newRepositorySystemSession(RepositorySystem system, String localRepoPath);
}
