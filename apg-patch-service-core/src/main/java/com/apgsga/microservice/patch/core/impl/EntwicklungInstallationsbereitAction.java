package com.apgsga.microservice.patch.core.impl;

import com.apgsga.artifact.query.ArtifactDependencyResolver;
import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.core.commands.CommandRunner;
import com.apgsga.microservice.patch.core.commands.CommandRunnerFactory;
import com.apgsga.microservice.patch.core.impl.jenkins.JenkinsClient;
import com.apgsga.microservice.patch.exceptions.Asserts;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

public class EntwicklungInstallationsbereitAction implements PatchAction {
	protected static final Log LOGGER = LogFactory.getLog(EntwicklungInstallationsbereitAction.class.getName());

	private final SimplePatchContainerBean patchContainer;

	private CommandRunnerFactory jschSessionFactory;

	private final JenkinsClient jenkinsPatchClient;

	private final ArtifactDependencyResolver dependencyResolver;

	public EntwicklungInstallationsbereitAction(SimplePatchContainerBean patchContainer) {
		super();
		this.patchContainer = patchContainer;
		this.jschSessionFactory = patchContainer.getJschSessionFactory();
		this.jenkinsPatchClient = patchContainer.getJenkinsClient();
		this.dependencyResolver = patchContainer.getDependecyResolver();
	}

	@Override
	public String executeToStateAction(String patchNumber, String toAction, Map<String, String> parameter) {
		LOGGER.info("Running EntwicklungInstallationsbereitAction, with: " + patchNumber + ", " + toAction
				+ ", and parameters: " + parameter.toString());
		Patch patch = patchContainer.getRepo().findById(patchNumber);
		Asserts.notNull(patch, "EntwicklungInstallationsbereitAction.patch.exists.assert",
				new Object[] { patchNumber, toAction });
		createAndSaveTagForPatch(patch);
		CommandRunner jschSession = jschSessionFactory.create();
		TaskEntwicklungInstallationsbereit.create(jschSession, patch, dependencyResolver,
				jenkinsPatchClient, patchContainer.getRepo()).run();
		patchContainer.executeStateTransitionActionInDb(patchNumber, Long.valueOf(patchContainer.getMetaInfoRepo().findStatus(toAction)));
		return "Ok: Created Patch Tag for " + patch.getPatchNummer();
	}

	private void createAndSaveTagForPatch(Patch patch) {
		patch.incrementTagNr();
		Integer tagNr = patch.getTagNr();
		String patchBranch = patch.getDbPatchBranch();
		patch.setPatchTag(patchBranch + "_" + tagNr.toString());
		patchContainer.getRepo().savePatch(patch);
	}
}