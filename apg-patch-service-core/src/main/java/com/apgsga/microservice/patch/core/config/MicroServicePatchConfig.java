package com.apgsga.microservice.patch.core.config;

import com.apgsga.artifact.query.ArtifactDependencyResolver;
import com.apgsga.artifact.query.ArtifactManager;
import com.apgsga.artifact.query.RepositorySystemFactory;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.api.PatchSystemMetaInfoPersistence;
import com.apgsga.microservice.patch.core.commands.CommandRunnerFactory;
import com.apgsga.microservice.patch.core.commands.JschSessionCmdRunnerFactory;
import com.apgsga.microservice.patch.core.commands.LoggingMockSshRunnerFactory;
import com.apgsga.microservice.patch.core.commands.ProcessBuilderCmdRunnerFactory;
import com.apgsga.microservice.patch.core.impl.PatchActionExecutorFactory;
import com.apgsga.microservice.patch.core.impl.PatchActionExecutorFactoryImpl;
import com.apgsga.microservice.patch.core.impl.jenkins.JenkinsClient;
import com.apgsga.microservice.patch.core.impl.jenkins.JenkinsClientImpl;
import com.apgsga.microservice.patch.core.impl.jenkins.JenkinsMockClient;
import com.apgsga.microservice.patch.core.impl.persistence.FilePatchSystemMetaInfoPersistence;
import com.apgsga.microservice.patch.core.impl.persistence.FilebasedPatchPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;

@Configuration
public class MicroServicePatchConfig {

	@Value("${vcs.host:cvs.apgsga.ch}")
	private String vcsHost;

	@Value("${vcs.user:}")
	private String vcsUser;

	@Value("${json.db.location:db}")
	private String dbLocation;

	@Value("${json.meta.info.db.location:metaInfoDb}")
	private String metaInfoDbLocation;

	@Value("${json.db.work.location:work}")
	private String workDirLocation;

	@Value("${json.meta.info.db.work.location:metaInfoWork}")
	private String metaInfoWorkDirLocation;

	@Value("${jenkins.host:https://jenkins.apgsga.ch/}")
	private String jenkinsHost;

	@Value("${jenkins.ssh.port:53801}")
	private String jenkinsSshPort;

	@Value("${jenkins.user}")
	private String jenkinsUser;

	@Value("${jenkins.authkey}")
	private String jenkinsAuthKey;

	@Value("${maven.localrepo.location}")
	private String localRepo;

	@Value("${taskexecutor.corePoolSize:5}")
	private Integer corePoolSize;

	@Value("${taskexecutor.maxPoolSize:20}")
	private Integer maxPoolSize;

	@Value("${mavenrepo.user.name}")
	private String mavenRepoUsername;

	@Value("${mavenrepo.baseurl}")
	private String mavenRepoBaseUrl;

	@Value("${mavenrepo.name}")
	private String mavenRepoName;

	@Value("${mavenrepo.user.encryptedPwd}")
	private String mavenRepoUserEncryptedPwd;

	@Value("${mavenrepo.user.decryptpwd.key:}")
	private String mavenRepoUserDecryptKey;

	@Bean(name = "patchPersistence")
	public PatchPersistence patchFilebasePersistence() throws IOException {
		final ResourceLoader rl = new FileSystemResourceLoader();
		Resource dbStorabe = rl.getResource(dbLocation);
		Resource workDir = rl.getResource(workDirLocation);
		final PatchPersistence per = new FilebasedPatchPersistence(dbStorabe, workDir);
		return per;
	}
	
	@Bean(name = "patchMetaInfoPersistence")
	public PatchSystemMetaInfoPersistence patchSystemMetaInfoPersistence() throws IOException {
		final ResourceLoader rl = new FileSystemResourceLoader();
		Resource metaInfoDbStorage = rl.getResource(metaInfoDbLocation);
		Resource metaInfoWorkdir = rl.getResource(metaInfoWorkDirLocation);
		final PatchSystemMetaInfoPersistence metaInfoPer = new FilePatchSystemMetaInfoPersistence(metaInfoDbStorage,metaInfoWorkdir);
		return metaInfoPer;
	}

	@Bean(name = "dependencyResolver")
	@Profile("live")
	public ArtifactDependencyResolver dependencyResolver() {
		return ArtifactDependencyResolver.create(localRepo, repositorySystemFactory());
	}

	@Bean(name = "dependencyResolver")
	@Profile("mock")
	public ArtifactDependencyResolver mockDependencyResolver() {
		return ArtifactDependencyResolver.createMock(localRepo);
	}

	@Bean(name = "repositorySystemFactory")
	public RepositorySystemFactory repositorySystemFactory() {
		return RepositorySystemFactory.create(mavenRepoBaseUrl,
				mavenRepoName,
				mavenRepoUsername,
				mavenRepoUserEncryptedPwd,
				mavenRepoUserDecryptKey);
	}

	@Bean(name = "artifactManager")
	@Profile({ "live", "mavenRepo" })
	public ArtifactManager artifactManager() {
		return ArtifactManager.create(localRepo, repositorySystemFactory());
	}

	@Bean(name = "artifactManager")
	@Profile("mockMavenRepo")
	public ArtifactManager mockArtifactManager() {
		return ArtifactManager.createMock(localRepo);
	}

	@Bean(name = "jenkinsBean")
	@Profile("live")
	public JenkinsClient jenkinsPatchClient() {
		final ResourceLoader rl = new FileSystemResourceLoader();
		Resource rDbLocation = rl.getResource(dbLocation);
		return new JenkinsClientImpl(rDbLocation, jenkinsHost, jenkinsSshPort, jenkinsUser, jenkinsAuthKey, threadPoolTaskExecutor());
	}

	@Bean(name = "vcsCmdRunnerFactory")
	@Profile({ "live", "remotecvs" })
	public CommandRunnerFactory jsessionFactory() {
		return new JschSessionCmdRunnerFactory(vcsUser, vcsHost);
	}

	@Bean(name = "vcsCmdRunnerFactory")
	@Profile({ "live", "localcvs" })
	public CommandRunnerFactory vcsLocalFactory() {
		return new ProcessBuilderCmdRunnerFactory();
	}

	@Bean(name = "jenkinsBean")
	@Profile("mock")
	public JenkinsClient jenkinsPatchClientMock() {
		return new JenkinsMockClient();
	}

	@Bean(name = "vcsCmdRunnerFactory")
	@Profile("mock")
	public CommandRunnerFactory jsessionFactoryMock() {
		return new LoggingMockSshRunnerFactory();
	}

	@Bean(name = "groovyActionFactory")
	@Profile({ "groovyactions" })
	public PatchActionExecutorFactory groovyPatchActionFactory() {
		return new PatchActionExecutorFactoryImpl();
	}

	@Bean(name = "taskExecutor")
	public TaskExecutor threadPoolTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(corePoolSize);
		executor.setMaxPoolSize(maxPoolSize);
		executor.setThreadNamePrefix("PatchServiceThreadExecutor");
		executor.setKeepAliveSeconds(1200);
		executor.initialize();
		return executor;
	}

}
