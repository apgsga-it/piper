package com.apgsga.microservice.patch.server.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.util.Assert;

import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchOpService;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.server.impl.jenkins.JenkinsPatchClient;
import com.apgsga.microservice.patch.server.impl.ssh.JschCvsSession;
import com.apgsga.microservice.patch.server.impl.ssh.JschSession;
import com.apgsga.microservice.patch.server.impl.ssh.JschSessionFactory;

public class ActionToEntwicklungInstallationsbereit implements ActionExecuteStateTransition {

	private final PatchPersistence repo;

	private JschSessionFactory jschSessionFactory;
	
	private final JenkinsPatchClient jenkinsPatchClient;

	public ActionToEntwicklungInstallationsbereit(SimplePatchContainerBean patchContainer) {
		super();
		this.repo = patchContainer.getRepo();
		this.jschSessionFactory = patchContainer.getJschSessionFactory();
		this.jenkinsPatchClient = patchContainer.getJenkinsClient();
	}

	@Override
	public void executeStateTransitionAction(String patchNumber) {
		Patch patch = repo.findById(patchNumber);
		Assert.notNull(patch, "Patch : <" + patchNumber + "> not found");
		createAndSaveTagForPatch(patch);
		JschSession jschSession = jschSessionFactory.create();
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.execute(new Runnable() {

			@Override
			public void run() {
				jschSession.connect();;
				tagDbObjects(jschSession,patch);
				tagJavaModules(jschSession,patch);
				jschSession.disconnect();
				jenkinsPatchClient.startProdPatchPipeline(patch);
			}
			
		});
		executorService.shutdown();
	

	}

	private void tagJavaModules(JschSession jschSession,Patch patch) {
		final StringBuffer cmdBuffer = new StringBuffer();
		cmdBuffer.append("cvs rtag -r " + patch.getMicroServiceBranch() + " " + patch.getPatchTag() + " ");
		patch.getMavenArtifacts().forEach(artifact -> cmdBuffer.append(artifact.getName() + " "));
		jschSession.execCommand(cmdBuffer.toString());
	}

	private void tagDbObjects(JschSession jschSession,Patch patch) {
		final StringBuffer cmdBuffer = new StringBuffer();
		cmdBuffer.append("cvs rtag -r " + patch.getProdBranch() + " " + patch.getPatchTag() + " ");
		patch.getDbObjects().forEach(dbObject -> cmdBuffer.append(dbObject.asFullPath() + " "));
		jschSession.execCommand(cmdBuffer.toString());
	}

	private void createAndSaveTagForPatch(Patch patch) {
		patch.incrementTagNr();
		Integer tagNr = patch.getTagNr();
		String patchBranch = patch.getDbPatchBranch();
		patch.setPatchTag(patchBranch + "_" + tagNr.toString());
		repo.save(patch);
	}

}
