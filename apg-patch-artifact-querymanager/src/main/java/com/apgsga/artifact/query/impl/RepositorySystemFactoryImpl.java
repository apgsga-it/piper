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

import com.apgsga.artifact.query.RepositorySystemFactory;
import com.google.common.collect.Lists;


public class RepositorySystemFactoryImpl implements RepositorySystemFactory {
	
	private String mavenRepoUsername;
	private String mavenRepoBaseUrl;
	private String mavenRepoName;
	private String mavenRepoUserPwd;
	
	public RepositorySystemFactoryImpl(){
		
	}
	
	public RepositorySystemFactoryImpl(String mavenRepoUsername, String mavenRepoBaseUrl, String mavenRepoName, String mavenRepoUserPwd) {
		this.mavenRepoBaseUrl = mavenRepoBaseUrl;
		this.mavenRepoName = mavenRepoName;
		this.mavenRepoUsername = mavenRepoUsername;
		this.mavenRepoUserPwd = mavenRepoUserPwd;
	}

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
		remoteRepos.add(newCentralRepository("central", mavenRepoBaseUrl + "/" + mavenRepoName));
		return new ArrayList<RemoteRepository>(remoteRepos);
	}

	private RemoteRepository newCentralRepository(String name, String url) {
        Authentication auth = new AuthenticationBuilder().addUsername(mavenRepoUsername).addPassword( this.mavenRepoUserPwd ).build();
		return new RemoteRepository.Builder(name, "default", url).setAuthentication( auth ).build();
	}

	public String getMavenRepoUsername() {
		return mavenRepoUsername;
	}

	public void setMavenRepoUsername(String mavenRepoUsername) {
		this.mavenRepoUsername = mavenRepoUsername;
	}

	public String getMavenRepoBaseUrl() {
		return mavenRepoBaseUrl;
	}

	public void setMavenRepoBaseUrl(
			String mavenRepoBaseUrl) {
		this.mavenRepoBaseUrl = mavenRepoBaseUrl;
	}

	public String getMavenRepoName() {
		return mavenRepoName;
	}

	public void setMavenRepoName(String mavenRepoName) {
		this.mavenRepoName = mavenRepoName;
	}

}
