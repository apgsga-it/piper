package com.apgsga.artifact.query.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;

import com.google.common.collect.Lists;

public class RepositorySystemFactory {
	private static final String HTTP_MAVENREPO_APGSGA_CH_NEXUS_CONTENT_GROUPS_PUBLIC = "http://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga/public";
	private static final String HTTP_MAVENREPO_APGSGA_CH_NEXUS_CONTENT_GROUPS_SNAPSHOT = "http://artifactory4t4apgsga.jfrog.io/artifactory4t4apgsga/public-snapshot";

	public static RepositorySystem newRepositorySystem() {
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

	public static DefaultRepositorySystemSession newRepositorySystemSession(RepositorySystem system,
			String localRepoPath) {
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

		LocalRepository localRepo = new LocalRepository(localRepoPath);
		session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

		session.setTransferListener(new Slf4jTransferListener());
		session.setRepositoryListener(new Slf4jRepositoryListener());

		return session;
	}

	public static List<RemoteRepository> newRepositories(RepositorySystem system, RepositorySystemSession session) {
		List<RemoteRepository> remoteRepos = Lists.newArrayList();
		remoteRepos.add(newCentralRepository("central", HTTP_MAVENREPO_APGSGA_CH_NEXUS_CONTENT_GROUPS_PUBLIC));
		remoteRepos.add(newCentralRepository("snapshots", HTTP_MAVENREPO_APGSGA_CH_NEXUS_CONTENT_GROUPS_PUBLIC));
		return new ArrayList<RemoteRepository>(remoteRepos);
	}

	private static RemoteRepository newCentralRepository(String name, String url) {
		return new RemoteRepository.Builder(name, "default", url).build();
	}

}
