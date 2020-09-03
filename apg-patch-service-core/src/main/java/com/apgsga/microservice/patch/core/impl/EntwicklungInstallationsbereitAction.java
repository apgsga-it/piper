package com.apgsga.microservice.patch.core.impl;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.task.TaskExecutor;

import com.apgsga.artifact.query.ArtifactDependencyResolver;
import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.exceptions.Asserts;
import com.apgsga.microservice.patch.core.impl.jenkins.JenkinsClient;
import com.apgsga.microservice.patch.core.commands.CommandRunner;
import com.apgsga.microservice.patch.core.commands.CommandRunnerFactory;

public class EntwicklungInstallationsbereitAction implements PatchAction {
	protected static final Log LOGGER = LogFactory.getLog(EntwicklungInstallationsbereitAction.class.getName());

	private final PatchPersistence repo;

	private CommandRunnerFactory jschSessionFactory;

	private final JenkinsClient jenkinsPatchClient;

	private final ArtifactDependencyResolver dependencyResolver;

	private final TaskExecutor threadExecutor;

	public EntwicklungInstallationsbereitAction(SimplePatchContainerBean patchContainer) {
		super();
		this.repo = patchContainer.getRepo();
		this.jschSessionFactory = patchContainer.getJschSessionFactory();
		this.jenkinsPatchClient = patchContainer.getJenkinsClient();
		this.dependencyResolver = patchContainer.getDependecyResolver();
		this.threadExecutor = patchContainer.getThreadExecutor();
	}

	@Override
	public String executeToStateAction(String patchNumber, String toAction, Map<String, String> parameter) {
		LOGGER.info("Running EntwicklungInstallationsbereitAction, with: " + patchNumber + ", " + toAction
				+ ", and parameters: " + parameter.toString());
		Patch patch = repo.findById(patchNumber);
		Asserts.notNull(patch, "EntwicklungInstallationsbereitAction.patch.exists.assert",
				new Object[] { patchNumber, toAction });
		createAndSaveTagForPatch(patch);
		CommandRunner jschSession = jschSessionFactory.create();
		threadExecutor.execute(TaskEntwicklungInstallationsbereit.create(jschSession, patch, dependencyResolver,
				jenkinsPatchClient, repo));
		return "Ok: Created Patch Tag and started Prod Patch Pipeline for: " + patch.getPatchNummer();
	}

	private void createAndSaveTagForPatch(Patch patch) {
		patch.incrementTagNr();
		Integer tagNr = patch.getTagNr();
		String patchBranch = patch.getDbPatchBranch();
		patch.setPatchTag(patchBranch + "_" + tagNr.toString());
		repo.savePatch(patch);
	}

}
