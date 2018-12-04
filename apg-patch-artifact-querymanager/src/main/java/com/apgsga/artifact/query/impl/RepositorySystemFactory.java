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
	// TODO (che, 9.3 ) : Temporory fix
	private String REPO_USER;
	private String HTTP_MAVENREPO_APGSGA_CH_NEXUS_CONTENT_GROUPS_PUBLIC;

//	private RepositorySystemFactory() {
//	}

	public RepositorySystem newRepositorySystem() {
//		REPO_USER = repoUser;
//		HTTP_MAVENREPO_APGSGA_CH_NEXUS_CONTENT_GROUPS_PUBLIC = mavenRepoUrl;
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
		remoteRepos.add(newCentralRepository("central", HTTP_MAVENREPO_APGSGA_CH_NEXUS_CONTENT_GROUPS_PUBLIC));
		return new ArrayList<RemoteRepository>(remoteRepos);
	}

	private RemoteRepository newCentralRepository(String name, String url) {
		String repoPasswd = System.getenv("REPO_RO_PASSWD"); 
		Preconditions.checkNotNull(repoPasswd,"Repo password should'nt be null");
        Authentication auth = new AuthenticationBuilder().addUsername(REPO_USER).addPassword( repoPasswd ).build();
		return new RemoteRepository.Builder(name, "default", url).setAuthentication( auth ).build();
	}

	public String getREPO_USER() {
		return REPO_USER;
	}

	public void setREPO_USER(String rEPO_USER) {
		REPO_USER = rEPO_USER;
	}

	public String getHTTP_MAVENREPO_APGSGA_CH_NEXUS_CONTENT_GROUPS_PUBLIC() {
		return HTTP_MAVENREPO_APGSGA_CH_NEXUS_CONTENT_GROUPS_PUBLIC;
	}

	public void setHTTP_MAVENREPO_APGSGA_CH_NEXUS_CONTENT_GROUPS_PUBLIC(
			String hTTP_MAVENREPO_APGSGA_CH_NEXUS_CONTENT_GROUPS_PUBLIC) {
		HTTP_MAVENREPO_APGSGA_CH_NEXUS_CONTENT_GROUPS_PUBLIC = hTTP_MAVENREPO_APGSGA_CH_NEXUS_CONTENT_GROUPS_PUBLIC;
	}

}
