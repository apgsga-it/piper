package com.apgsga.artifact.query.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.repository.AuthenticationBuilder;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;


public class RepositorySystemFactory {
	private String repoUser;
	private String httpPublicArtifactoryMavenRepo;

	public RepositorySystem newRepositorySystem() {
		DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
		locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
		locator.addService(TransporterFactory.class, FileTransporterFactory.class);
		locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
		locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
			@Override
			public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
				exception.printStackTrace();
			}
		});

		return locator.getService(RepositorySystem.class);
	}

	public DefaultRepositorySystemSession newRepositorySystemSession(RepositorySystem system,
			String localRepoPath) {
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

		LocalRepository localRepo = new LocalRepository(localRepoPath);
		session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

		session.setTransferListener(new Slf4jTransferListener());
		session.setRepositoryListener(new Slf4jRepositoryListener());

		return session;
	}

	public List<RemoteRepository> newRepositories() {
		List<RemoteRepository> remoteRepos = Lists.newArrayList();
		remoteRepos.add(newCentralRepository("central", httpPublicArtifactoryMavenRepo));
		return new ArrayList<RemoteRepository>(remoteRepos);
	}

	private RemoteRepository newCentralRepository(String name, String url) {
		String repoPasswd = System.getenv("REPO_RO_PASSWD"); 
		Preconditions.checkNotNull(repoPasswd,"Repo password should'nt be null");
        Authentication auth = new AuthenticationBuilder().addUsername(repoUser).addPassword( repoPasswd ).build();
		return new RemoteRepository.Builder(name, "default", url).setAuthentication( auth ).build();
	}

	public String getRepoUser() {
		return repoUser;
	}

	public void setRepoUser(String repUser) {
		repoUser = repUser;
	}

	public String getHttpPublicArtifactoryMavenRepo() {
		return httpPublicArtifactoryMavenRepo;
	}

	public void setHttpPublicArtifactoryMavenRepo(
			String p_httpPublicArtifactoryMavenRepo) {
		httpPublicArtifactoryMavenRepo = p_httpPublicArtifactoryMavenRepo;
	}

}
