package com.apgsga.microservice.patch.server.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.apgsga.artifact.query.ArtifactManager;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.server.impl.GroovyScriptActionExecutorFactory;
import com.apgsga.microservice.patch.server.impl.PatchActionExecutorFactory;
import com.apgsga.microservice.patch.server.impl.jenkins.JenkinsPatchClient;
import com.apgsga.microservice.patch.server.impl.jenkins.JenkinsPatchClientImpl;
import com.apgsga.microservice.patch.server.impl.jenkins.JenkinsPatchMockClient;
import com.apgsga.microservice.patch.server.impl.persistence.FilebasedPatchPersistence;
import com.apgsga.microservice.patch.server.impl.vcs.JschSessionCmdRunnerFactory;
import com.apgsga.microservice.patch.server.impl.vcs.LoggingMockVcsRunnerFactory;
import com.apgsga.microservice.patch.server.impl.vcs.ProcessBuilderCmdRunnerFactory;
import com.apgsga.microservice.patch.server.impl.vcs.VcsCommandRunnerFactory;

@Configuration
@EnableWebMvc
public class MicroServicePatchConfig {

	@Value("${vcs.host:cvs.apgsga.ch}")
	private String vcsHost;

	@Value("${vcs.user:}")
	private String vcsUser;

	@Value("${vcs.password:}")
	private String vcsPassword;

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

	@Value("${patch.action.script.location:classpath:executePatchAction.groovy}")
	private String groovyScriptFile;

	@Bean(name = "patchPersistence")
	public PatchPersistence patchFilebasePersistence() throws IOException {
		final ResourceLoader rl = new FileSystemResourceLoader();
		Resource dbStorabe = rl.getResource(dbLocation);
		Resource workDir = rl.getResource(workDirLocation);
		final PatchPersistence per = new FilebasedPatchPersistence(dbStorabe, workDir);
		per.init();
		return per;
	}

	@Bean(name = "artifactManager")
	@Profile("live")
	public ArtifactManager artifactManager() {
		return ArtifactManager.create(localRepo);
	}

	@Bean(name = "artifactManager")
	@Profile("mock")
	public ArtifactManager mockArtifactManager() {
		return ArtifactManager.createMock(localRepo);
	}

	@Bean(name = "jenkinsBean")
	@Profile("live")
	public JenkinsPatchClient jenkinsPatchClient() {
		return new JenkinsPatchClientImpl(jenkinsHost, jenkinsUser, jenkinsAuthKey);
	}

	@Bean(name = "vcsCmdRunnerFactory")
	@Profile({ "live", "remotecvs" })
	public VcsCommandRunnerFactory jsessionFactory() {
		return new JschSessionCmdRunnerFactory(vcsUser, vcsPassword, vcsHost);
	}

	@Bean(name = "vcsCmdRunnerFactory")
	@Profile({ "live", "localcvs" })
	public VcsCommandRunnerFactory vcsLocalFactory() {
		return new ProcessBuilderCmdRunnerFactory();
	}

	@Bean(name = "jenkinsBean")
	@Profile("mock")
	public JenkinsPatchClient jenkinsPatchClientMock() {
		return new JenkinsPatchMockClient();
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

}
