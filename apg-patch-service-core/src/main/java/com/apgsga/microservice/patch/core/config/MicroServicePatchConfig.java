package com.apgsga.microservice.patch.core.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.apgsga.artifact.query.ArtifactDependencyResolver;
import com.apgsga.artifact.query.ArtifactManager;
import com.apgsga.artifact.query.RepositorySystemFactory;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.core.impl.GroovyScriptActionExecutorFactory;
import com.apgsga.microservice.patch.core.impl.PatchActionExecutorFactory;
import com.apgsga.microservice.patch.core.impl.jenkins.JenkinsClient;
import com.apgsga.microservice.patch.core.impl.jenkins.JenkinsClientImpl;
import com.apgsga.microservice.patch.core.impl.jenkins.JenkinsMockClient;
import com.apgsga.microservice.patch.core.impl.persistence.FilebasedPatchPersistence;
import com.apgsga.microservice.patch.core.impl.vcs.JschSessionCmdRunnerFactory;
import com.apgsga.microservice.patch.core.impl.vcs.LoggingMockVcsRunnerFactory;
import com.apgsga.microservice.patch.core.impl.vcs.ProcessBuilderCmdRunnerFactory;
import com.apgsga.microservice.patch.core.impl.vcs.VcsCommandRunnerFactory;

@Configuration
public class MicroServicePatchConfig {

	@Value("${vcs.host:cvs.apgsga.ch}")
	private String vcsHost;

	@Value("${vcs.user:}")
	private String vcsUser;

	@Value("${json.db.location:db}")
	private String dbLocation;

	@Value("${json.db.work.location:work}")
	private String workDirLocation;

	@Value("${jenkins.host:https://jenkins.apgsga.ch/}")
	private String jenkinsHost;

	@Value("${jenkins.user}")
	private String jenkinsUser;

	@Value("${jenkins.authkey}")
	private String jenkinsAuthKey;

	@Value("${maven.localrepo.location}")
	private String localRepo;

	@Value("${config.common.location:/etc/opt/apg-patch-common}")
	private String configCommon;

	@Value("${config.common.targetSystemFile:TargetSystemMappings.json}")
	private String targetSystemFile;

	@Value("${patch.action.script.location:classpath:groovyScript/executePatchAction.groovy}")
	private String groovyScriptFile;

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
		per.init();
		return per;
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
		return new JenkinsClientImpl(rDbLocation, jenkinsHost, jenkinsUser, jenkinsAuthKey, threadPoolTaskExecutor());
	}

	@Bean(name = "vcsCmdRunnerFactory")
	@Profile({ "live", "remotecvs" })
	public VcsCommandRunnerFactory jsessionFactory() {
		return new JschSessionCmdRunnerFactory(vcsUser, vcsHost);
	}

	@Bean(name = "vcsCmdRunnerFactory")
	@Profile({ "live", "localcvs" })
	public VcsCommandRunnerFactory vcsLocalFactory() {
		return new ProcessBuilderCmdRunnerFactory();
	}

	@Bean(name = "jenkinsBean")
	@Profile("mock")
	public JenkinsClient jenkinsPatchClientMock() {
		return new JenkinsMockClient();
	}

	@Bean(name = "vcsCmdRunnerFactory")
	@Profile("mock")
	public VcsCommandRunnerFactory jsessionFactoryMock() {
		return new LoggingMockVcsRunnerFactory();
	}

	@Bean(name = "groovyActionFactory")
	@Profile({ "groovyactions" })
	public PatchActionExecutorFactory groovyPatchActionFactory() {
		return new GroovyScriptActionExecutorFactory(configCommon, targetSystemFile, groovyScriptFile);
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
