package com.apgsga.microservice.patch.core.impl;

import com.apgsga.artifact.query.ArtifactManager;
import com.apgsga.microservice.patch.api.*;
import com.apgsga.microservice.patch.core.commands.CommandRunner;
import com.apgsga.microservice.patch.core.commands.CommandRunnerFactory;
import com.apgsga.microservice.patch.core.commands.JschSessionCmdRunnerFactory;
import com.apgsga.microservice.patch.core.commands.patch.vcs.PatchSshCommand;
import com.apgsga.microservice.patch.core.impl.jenkins.JenkinsClient;
import com.apgsga.microservice.patch.exceptions.Asserts;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.apgsga.patch.db.integration.api.PatchRdbms;
import com.apgsga.microservice.patch.api.NotificationParameters;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("ServerBean")
public class SimplePatchContainerBean implements PatchService, PatchOpService {

	protected static final Log LOGGER = LogFactory.getLog(SimplePatchContainerBean.class.getName());

	@Autowired
	@Qualifier("patchPersistence")
	private PatchPersistence repo;

	@Autowired
	private JenkinsClient jenkinsClient;

	@Autowired
	private ArtifactManager am;

	@Autowired
	private CommandRunnerFactory sshCommandRunnerFactory;

	@Autowired
	private TaskExecutor threadExecutor;

	public SimplePatchContainerBean() {
		super();
	}

	public SimplePatchContainerBean(PatchPersistence repo) {
		super();
		this.repo = repo;
	}

	@Override
	public List<String> listDbModules() {
		DbModules dbModules = repo.getDbModules();
		if (dbModules == null)
			return Lists.newArrayList();
		return dbModules.getDbModules();
	}

	@Override
	public List<MavenArtifact> listMavenArtifacts(String serviceName, SearchCondition filter) {
		ServiceMetaData data = repo.getServiceMetaDataByName(serviceName);
		List<MavenArtifact> mavenArtFromStarterList;
		try {
			mavenArtFromStarterList = am.getAllDependencies(
					data.getBaseVersionNumber() + "." + data.getRevisionMnemoPart() + "-SNAPSHOT", filter);
		} catch (DependencyResolutionException | ArtifactResolutionException | IOException | XmlPullParserException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException(
					"SimplePatchContainerBean.listMavenArtifacts.exception",
					new Object[] { e.getMessage(), serviceName.toString() }, e);
		}

		return mavenArtFromStarterList;
	}

	@Override
	public List<MavenArtifact> listMavenArtifacts(String serviceName) {
		return listMavenArtifacts(serviceName, SearchCondition.APPLICATION);
	}

	@Override
	public List<ServiceMetaData> listServiceData() {
		return repo.getServicesMetaData().getServicesMetaData();
	}

	@Override
	public Patch findById(String patchNummer) {
		return repo.findById(patchNummer);
	}
	
	@Override
	public PatchLog findPatchLogById(String patchNummer) {
		return repo.findPatchLogById(patchNummer);
	}

	@Override
	public List<Patch> findByIds(List<String> patchIds) {
		List<Patch> patches = Lists.newArrayList();
		patchIds.forEach(patchId -> {
			Patch p = findById(patchId);
			patches.add(p);
		});
		return patches;
	}

	@Override
	public Patch save(Patch patch) {
		Asserts.notNull(patch, "SimplePatchContainerBean.save.patchobject.notnull.assert", new Object[] {});
		Asserts.notNullOrEmpty(patch.getPatchNumber(),
				"SimplePatchContainerBean.save.patchnumber.notnullorempty.assert", new Object[] { patch.toString() });
		preProcessSave(patch);
		repo.savePatch(patch);
		return patch;
	}

	@Override
	public void savePatchLog(String patchNumber, PatchLogDetails logDetails) {
		repo.savePatchLog(patchNumber,logDetails);
	}

	@Override
	public void log(String patchNumber, PatchLogDetails logDetails) {
		Asserts.notNullOrEmpty(patchNumber, "SimplePatchContainerBean.log.patchnumber.isnullorempty", new Object[] {});
		Asserts.notNull(repo.findById(patchNumber), "SimplePatchContainerBean.log.patch.not.exist", new Object[] {patchNumber});
		repo.savePatchLog(patchNumber, logDetails);
	}

	private void preProcessSave(Patch patch) {
		if (!repo.patchExists(patch.getPatchNumber())) {
			createBranchForDbModules(patch);
			jenkinsClient.createPatchPipelines(patch);
		}

	}

	@Override
	public void remove(Patch patch) {
		repo.removePatch(patch);
	}

	private void createBranchForDbModules(Patch patch) {
		DbModules dbModules = repo.getDbModules();
		if (dbModules == null) {
			LOGGER.warn("Could not create CVS DB-Branch for patch " + patch.getPatchNumber() + " as no dbModules are define!");
			return;
		}
		LOGGER.info("Create CVS DB-Branch for patch " + patch.getPatchNumber());
		final CommandRunner sshCommandRunner = sshCommandRunnerFactory.create();
		sshCommandRunner.preProcess();
		sshCommandRunner.run(PatchSshCommand.createCreatePatchBranchCmd(patch.getDbPatchBranch(), patch.getProdBranch(),
				dbModules.getDbModules()));
		sshCommandRunner.postProcess();
	}

	@Override
	public List<String> listOnDemandTargets() {
		OnDemandTargets onDemandTargets = getRepo().onDemandTargets();
		return onDemandTargets.getOnDemandTargets();
	}

	@Override
	public List<DbObject> listAllObjectsChangedForDbModule(String patchId, String searchString) {
		Patch patch = findById(patchId);
		Asserts.notNull(patch, "SimplePatchContainerBean.listAllObjectsChangedForDbModule.patch.exists.assert",
				new Object[] { patchId });
		DbModules dbModules = repo.getDbModules();
		if (dbModules == null) {
			return Lists.newArrayList();
		}
		final CommandRunner sshCommandRunner = sshCommandRunnerFactory.create();
		sshCommandRunner.preProcess();
		List<DbObject> dbObjects = Lists.newArrayList();
		for (String dbModule : dbModules.getDbModules()) {
			if (Strings.isNullOrEmpty(dbModule) || dbModule.contains(searchString)) {
				List<String> result = sshCommandRunner.run(PatchSshCommand
						.createDiffPatchModulesCmd(patch.getDbPatchBranch(), patch.getProdBranch(), dbModule));
				List<String> files = result.stream()
						.filter(s -> s.startsWith("Index: "))
						.map(s -> s.substring(7)).collect(Collectors.toList());
				files.stream().forEach(file -> {
					DbObject dbObject = DbObject.builder()
							.fileName(dbModule)
							.fileName(FilenameUtils.getName(file))
							.filePath(FilenameUtils.getPath(file)).build();
					dbObjects.add(dbObject);
				});

			}
		}
		sshCommandRunner.postProcess();
		return dbObjects;
	}

	@Override
	public List<DbObject> listAllObjectsForDbModule(String patchNumber, String searchString, String username) {
		String suffixForCoFolder = username + "_" + new Date().getTime();
		LOGGER.info("Searching all DB Objects for user " + username);
		return doListAllSqlObjectsForDbModule(patchNumber, searchString, suffixForCoFolder);
	}

	@Override
	public List<DbObject> listAllObjectsForDbModule(String patchNumber, String searchString) {
		String suffixForCoFolder = String.valueOf(new Date().getTime());
		LOGGER.info("Searching all DB Objects without any specific user");
		return doListAllSqlObjectsForDbModule(patchNumber, searchString, suffixForCoFolder);
	}

	private List<DbObject> doListAllSqlObjectsForDbModule(String patchNumber, String searchString, String suffixForCoFolder) {
		Patch patch = findById(patchNumber);
		Asserts.notNull(patch, "SimplePatchContainerBean.listAllObjectsChangedForDbModule.patch.exists.assert",
				new Object[] { patchNumber });
		DbModules dbModules = repo.getDbModules();
		if (dbModules == null) {
			return Lists.newArrayList();
		}
		final CommandRunner sshCommandRunner = sshCommandRunnerFactory.create();
		sshCommandRunner.preProcess();
		List<DbObject> dbObjects = Lists.newArrayList();
		for (String dbModule : dbModules.getDbModules()) {
			if (dbModule.contains(searchString)) {
				String tempSubFolderName= "apg_patch_ui_temp_";
				String tmpDir = System.getProperty("java.io.tmpdir");
				String coFolder = tmpDir + "/" + tempSubFolderName + suffixForCoFolder;
				String additionalOptions = "-d " + coFolder;
				LOGGER.info("Temporary checkout folder for listing all DB Objects will be: " + coFolder);
				List<String> result = sshCommandRunner.run(PatchSshCommand.createCoCvsModuleToDirectoryCmd(patch.getDbPatchBranch(), patch.getProdBranch(), Lists.newArrayList(dbModule), additionalOptions));
				result.forEach(r -> {
					// JHE : In production, cvs is on a separated server, therefore we can't checkout, and parse the local result ...
					//		 We rely on the output given back from the CVS command, might not be the most robust solution :( ... but so far ok for a function which is not crucial.
					int startIndex = r.indexOf("U ")+"U ".length();
					String pathToResourceName = r.substring(startIndex, r.length()).trim().replaceFirst(suffixForCoFolder, "").replaceFirst(tmpDir + "/", "");
					DbObject dbObject = DbObject.builder()
							.fileName(dbModule)
							.fileName(FilenameUtils.getName(pathToResourceName))
							.filePath(FilenameUtils.getPath(dbModule + "/" + FilenameUtils.getPath(pathToResourceName.replaceFirst(tempSubFolderName,""))))
							.build();
					dbObjects.add(dbObject);
				});

				List<String> rmResult = sshCommandRunner.run(PatchSshCommand.createRmTmpCheckoutFolder(coFolder));
			}
		}
		sshCommandRunner.postProcess();
		return dbObjects;
	}

	@Override
	public void build(BuildParameter bp) {
		LOGGER.info("Build patch " + bp.getPatchNumber() + " for stage " + bp.getStageName() + " with successNotification=" + bp.getSuccessNotification() + " and errorNotification=" + bp.getErrorNotification());
		Patch patch = repo.findById(bp.getPatchNumber());
		Asserts.notNull(patch,"SimplePatchContainerBean.build.patch.exists.assert", new Object[] {bp.getPatchNumber()});
		String target = repo.targetFor(bp.getStageName());
		Asserts.notNullOrEmpty(target,"SimplePatchContainerBean.build.target.notnull", new Object[]{bp.getPatchNumber()});
		jenkinsClient.startProdBuildPatchPipeline(bp);
	}

	@Override
	public void setup(SetupParameter sp) {
		LOGGER.info("Setup started for Patch " + sp.getPatchNumber() + " with successNotification=" + sp.getSuccessNotification() + " and errorNotification=" + sp.getErrorNotification());
		Patch patch = repo.findById(sp.getPatchNumber());
		Asserts.notNull(patch, "SimplePatchContainerBean.patch.exists.assert",new Object[] { sp.getPatchNumber()});
		CommandRunner jschSession = getJschSessionFactory().create();
		PatchSetupTask.create(jschSession, patch, repo, sp).run();
	}


	@Override
	public void cleanLocalMavenRepo() {
		am.cleanLocalMavenRepo();

	}

	public PatchPersistence getRepo() {
		return repo;
	}

	public CommandRunnerFactory getJschSessionFactory() {
		return sshCommandRunnerFactory;
	}

	@Override
	public List<Patch> findWithObjectName(String objectName) {
		List<String> patchFiles = repo.listFiles("Patch");
		// Filter out "PatchLog" files, and extract Id from patch name
		List<String> patchFilesReduced = patchFiles.stream().filter(pat -> !pat.contains("PatchLog")).map(pat -> pat.substring(pat.indexOf("Patch")+"Patch".length(),pat.indexOf(".json"))).collect(Collectors.toList());
		return patchFilesReduced.stream().filter(p -> containsObject(p,objectName)).map(this::findById).collect(Collectors.toList());
	}

	@Override
	public void copyPatchFiles(Map<String,String> params) {

		//TODO JHE (18.11.2020) : empty for now as this is a deprecated method which will probably be deleted
	}

	@Override
	public List<String> patchIdsForStatus(String statusCode) {
		return this.repo.patchIdsForStatus(statusCode);
	}

	@Override
	public void notify(NotificationParameters params) {
		this.repo.notify(params);
	}

	private boolean containsObject(String patchNumber, String objectName) {
		Patch patch = findById(patchNumber);
		for(MavenArtifact ma : patch.retrieveAllArtifactsToPatch()) {
			if(ma.getArtifactId()!= null && ma.getArtifactId().toUpperCase().contains(objectName.toUpperCase()))
				return true;
		}
		for(DbObject dbo : patch.getDbObjects()) {
			if(dbo.getFileName() != null && dbo.getFileName().toUpperCase().contains(objectName.toUpperCase())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void startAssembleAndDeployPipeline(AssembleAndDeployParameters parameters) {
		LOGGER.info("assembleAndDeploy parameters will be completed based on following: " + parameters.toString());
		if(!parameters.getPatches().isEmpty()) {
			parameters.getPatches().forEach(patchNumber -> {
				Patch p = findById(patchNumber);
				Asserts.notNull(p,"SimplePatchContainerBean.startAssembleAndDeployPipeline.patch.isnull", new Object[]{patchNumber});
				p.getServices().forEach(service -> {
					// TODO (JHE, 15.12) : Move this whole transformation into JenkinsPipelinePreprocessor
					//TODO (JHE, 15.12) : Address the Multi Packager Scenario
					//TODO (JHE, 15.12) : Below just a quick fix , that it compiles
					parameters.addGradlePackageProjectAsVcsPath(repo.getServiceMetaDataByName(service.getServiceName()).getPackages().get(0).getPackagerName());
				});
			});
			jenkinsClient.startAssembleAndDeployPipeline(parameters);
		}
		else {
			LOGGER.warn("An assembleAndDeploy Pipeline job was requested without any Patch in the list. Parameters were: " + parameters.toString());
		}
	}

	@Override
	public void startInstallPipeline(String target) {
		// TOOO (JHE, CHE: 13.10) And String parameter as Json according to Pipeline Requirements
		jenkinsClient.startInstallPipeline(target,  "");
	}


}
