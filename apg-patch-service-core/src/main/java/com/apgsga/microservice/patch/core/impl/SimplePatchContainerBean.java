package com.apgsga.microservice.patch.core.impl;

import com.apgsga.artifact.query.ArtifactDependencyResolver;
import com.apgsga.artifact.query.ArtifactManager;
import com.apgsga.microservice.patch.api.*;
import com.apgsga.microservice.patch.core.commands.Command;
import com.apgsga.microservice.patch.core.commands.CommandRunner;
import com.apgsga.microservice.patch.core.commands.CommandRunnerFactory;
import com.apgsga.microservice.patch.core.commands.patch.vcs.PatchSshCommand;
import com.apgsga.microservice.patch.core.commands.patch.vcs.RmTmpCheckoutFolder;
import com.apgsga.microservice.patch.core.impl.jenkins.JenkinsClient;
import com.apgsga.microservice.patch.core.patch.conflicts.PatchConflict;
import com.apgsga.microservice.patch.core.patch.conflicts.PatchConflictCheckerFactory;
import com.apgsga.microservice.patch.core.patch.conflicts.PatchConflictsChecker;
import com.apgsga.microservice.patch.exceptions.Asserts;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
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
	private PatchConflictCheckerFactory conflictsCheckerFactory;

	@Autowired
	@Qualifier("vcsCmdRunnerFactory")
	private CommandRunnerFactory sshCommandRunnerFactory;

	@Autowired
	@Qualifier("dockerCmdRunnerFactory")
	private CommandRunnerFactory dockerCmdRunnerFactory;

	@Autowired
	private TaskExecutor threadExecutor;

	@Autowired
	private ArtifactDependencyResolver dependencyResolver;

	@Value("${db.patch.branch.prefix:Ms_patch_}")
	private String DB_PATCH_BRANCH_PREFIX;

	@Value("${db.prod.branch:prod}")
	private String DB_PROD_BRANCH;

	@Value("${cvs.check.for.branch.project.name:piperCheckForBranch}")
	private String PIPER_CHECK_FOR_BRANCH_PROJECT_NAME;

	@Value("${docker.tag.script.path:/opt/apg-patch-service-server/bin/ibusDockerTagNPush.sh}")
	private String DOCKER_TAG_SCRIPT_NAME;

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
		return am.listDependenciesInBom(data.getBomCoordinates(), filter);
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
	public Patch findById(String patchNumber) {
		return repo.findById(patchNumber);
	}
	
	@Override
	public PatchLog findPatchLogById(String patchNumber) {
		return repo.findPatchLogById(patchNumber);
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
		Asserts.notNull(patch, "Patch object null for save");
		Asserts.notNullOrEmpty(patch.getPatchNumber(),
				"Patch number  null or empty for save");
		// We're working with value object, therefore we eventually get a new object, if any field's value has been updated.
		patch = preProcessSave(patch);
		repo.savePatch(patch);
		return patch;
	}

	@Override
	public void savePatchLog(String patchNumber, PatchLogDetails logDetails) {
		repo.savePatchLog(patchNumber,logDetails);
	}

	@Override
	public void log(String patchNumber, PatchLogDetails logDetails) {
		Asserts.notNull(logDetails, "PatchLogDetails object null for log");
		Asserts.notNull(patchNumber, "PatchNumber null for log");
		Asserts.notNull(repo.findById(patchNumber), "Patch %s object does not exist for log", patchNumber);
		repo.savePatchLog(patchNumber, logDetails);
	}

	private Patch preProcessSave(Patch patch) {
		if (!repo.patchExists(patch.getPatchNumber())) {
			DBPatch dbPatch = DBPatch.builder().dbPatchBranch(DB_PATCH_BRANCH_PREFIX + patch.getPatchNumber()).prodBranch(DB_PROD_BRANCH).build();
			patch = patch.toBuilder().dbPatch(dbPatch).build();
			jenkinsClient.createPatchPipelines(patch);
			if(!dbPatchBranchExist(patch)) {
				LOGGER.warn("DB Patch Branch for patch " + patch.getPatchNumber() + " did not exist -> will now be created.");
				createBranchForDbModules(patch);
			}
		}
		return patch;
	}

	private boolean dbPatchBranchExist(Patch patch) {
		final CommandRunner sshCommandRunner = sshCommandRunnerFactory.create();
		sshCommandRunner.preProcess();

		String tempCheckoutFolder = PIPER_CHECK_FOR_BRANCH_PROJECT_NAME + "_" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());

		LOGGER.info("Verifying if DB Patch Branch for patch " + patch.getPatchNumber() + " exists. Checkout will be done remotely on CVS server within /home/svcCvsClient/" + tempCheckoutFolder);

		Command coCvsCmd = PatchSshCommand.createCoCvsModuleToDirectoryCmd(patch.getDbPatch().getDbPatchBranch(), patch.getDbPatch().getProdBranch(), Lists.newArrayList(PIPER_CHECK_FOR_BRANCH_PROJECT_NAME), "-d " + tempCheckoutFolder);
		List<String> result = sshCommandRunner.run(coCvsCmd);

		if(!result.contains("SSHExistStatus=0")) {
			return false;
		}

		// Delete the remote temp folder
		RmTmpCheckoutFolder rmFolderCmd = new RmTmpCheckoutFolder(tempCheckoutFolder);
		sshCommandRunner.run(rmFolderCmd);
		sshCommandRunner.postProcess();

		return true;
	}

	@Override
	public void remove(Patch patch) {
		repo.removePatch(patch);
	}

	private void createBranchForDbModules(Patch patch) {
		DbModules dbModules = repo.getDbModules();
		if (dbModules == null || dbModules.getDbModules().isEmpty()) {
			LOGGER.info("Could not create CVS DB-Branch for patch " + patch.getPatchNumber() + " as no dbModules are to patch!");
			return;
		}
		Asserts.notNullOrEmpty(patch.getDbPatch().getDbPatchBranch(), "DbPatchBranch is null or empty");
		Asserts.notNullOrEmpty(patch.getDbPatch().getProdBranch(), "Db Prod Branch is null or empty");
		LOGGER.info("Create CVS DB-Branch for patch " + patch.getPatchNumber());
		final CommandRunner sshCommandRunner = sshCommandRunnerFactory.create();
		sshCommandRunner.preProcess();
		sshCommandRunner.run(PatchSshCommand.createCreatePatchBranchCmd(patch.getDbPatch().getDbPatchBranch(), patch.getDbPatch().getProdBranch(),
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
		LOGGER.info("Searching all changed DB Objects for " + patchId + " with searchString" + searchString);
		Patch patch = findById(patchId);
		Asserts.notNull(patch,"Patch %s does not exist for listAllObjectsChangedForDbModule", patchId);
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
						.createDiffPatchModulesCmd(patch.getDbPatch().getDbPatchBranch(), patch.getDbPatch().getProdBranch(),null, Lists.newArrayList(dbModule)));
				List<String> files = result.stream()
						.filter(s -> s.startsWith("Index: "))
						.map(s -> s.substring(7)).collect(Collectors.toList());
				files.stream().forEach(file -> {
					DbObject dbObject = DbObject.builder()
							                    .moduleName(dbModule)
											    .fileName(FilenameUtils.getName(file))
												.filePath(FilenameUtils.getPath(file))
											    .build();
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
		Asserts.notNull(patch, "Patch %s does not exist for doListAllSqlObjectsForDbModule", patchNumber);
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
				List<String> result = sshCommandRunner.run(PatchSshCommand.createCoCvsModuleToDirectoryCmd(patch.getDbPatch().getDbPatchBranch(), patch.getDbPatch().getProdBranch(), Lists.newArrayList(dbModule), additionalOptions));
				result.forEach(r -> {
					// JHE : In production, cvs is on a separated server, therefore we can't checkout, and parse the local result ...
					//		 We rely on the output given back from the CVS command, might not be the most robust solution :( ... but so far ok for a function which is not crucial.
					int startIndex = r.indexOf("U ")+"U ".length();
					String pathToResourceName = r.substring(startIndex).trim().replaceFirst(suffixForCoFolder, "").replaceFirst(tmpDir + "/", "");
					DbObject dbObject = DbObject.builder()
							.moduleName(dbModule)
							.fileName(FilenameUtils.getName(pathToResourceName))
							.filePath(FilenameUtils.getPath(dbModule + "/" + FilenameUtils.getPath(pathToResourceName.replaceFirst(tempSubFolderName,""))))
							.build();
					dbObjects.add(dbObject);
				});

				sshCommandRunner.run(PatchSshCommand.createRmTmpCheckoutFolder(coFolder));
			}
		}
		sshCommandRunner.postProcess();
		return dbObjects;
	}

	@Override
	public void build(BuildParameter bp) {
		LOGGER.info("Build patch " + bp.getPatchNumber() + " for stage " + bp.getStageName() + " with successNotification=" + bp.getSuccessNotification() + " and errorNotification=" + bp.getErrorNotification());
		Patch patch = repo.findById(bp.getPatchNumber());
		Asserts.notNull(patch,"Patch %s does not exist for build", bp.getPatchNumber());
		String target = repo.targetFor(bp.getStageName());
		Asserts.notNullOrEmpty(target,"Target %s does not exist for build of Patch %s", bp.getStageName(), bp.getPatchNumber());
		jenkinsClient.startProdBuildPatchPipeline(bp);
	}

	@Override
	public void setup(SetupParameter sp) {
		LOGGER.info("Setup started for Patch " + sp.getPatchNumber() + " with successNotification=" + sp.getSuccessNotification() + " and errorNotification=" + sp.getErrorNotification());
		Patch patch = repo.findById(sp.getPatchNumber());
		Asserts.notNull(patch,"Patch %s does not exist for setup",  sp.getPatchNumber());
		CommandRunner jschSession = getJschSessionFactory().create();
		CommandRunner localSession = getDockerCmdRunnerFactory().create();
		PatchSetupTask.create(jschSession, localSession, patch, repo, sp, am, dependencyResolver,DOCKER_TAG_SCRIPT_NAME).run();
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

	public CommandRunnerFactory getDockerCmdRunnerFactory() {
		return dockerCmdRunnerFactory;
	}

	@Override
	public List<Patch> findWithObjectName(String objectName) {
		List<String> patchFiles = repo.listFiles("Patch");
		// Filter out "PatchLog" files, and extract Id from patch name
		List<String> patchFilesReduced = patchFiles.stream().filter(pat -> !pat.contains("PatchLog")).map(pat -> pat.substring(pat.indexOf("Patch")+"Patch".length(),pat.indexOf(".json"))).collect(Collectors.toList());
		return patchFilesReduced.stream().filter(p -> containsObject(p,objectName)).map(this::findById).collect(Collectors.toList());
	}

	@Override
	public void startOnDemandInstallation(OnDemandParameter params) {
		this.startOnDemandPipeline(params);
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
		for(DbObject dbo : patch.getDbPatch().getDbObjects()) {
			if(dbo.getFileName() != null && dbo.getFileName().toUpperCase().contains(objectName.toUpperCase())) {
				return true;
			}
		}
		for(String dockerService : patch.getDockerServices()) {
			if(dockerService.toUpperCase().contains(objectName.toUpperCase())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void startOnDemandPipeline(OnDemandParameter parameters) {
		LOGGER.info("Starting onDemand pipeline with following parameter: " + parameters.toString());
		Asserts.notNullOrEmpty(parameters.getPatchNumber(), "patchNumber required when starting onDemand pipeline");
		Asserts.notNullOrEmpty(parameters.getTarget(), "target required when starting onDemand pipeline");
		jenkinsClient.startOnDemandPipeline(parameters);
	}

	@Override
	public void startOnClonePipeline(OnCloneParameters parameters) {
		LOGGER.info("Starting onClone pipeline with following parameter: " + parameters.toString());
		Asserts.notNullOrEmpty(parameters.getSrc(), "Src is required when starting an onClone pipeline");
		Asserts.notNullOrEmpty(parameters.getTarget(), "target is required when starting an onClone pipeline");
		Asserts.isTrue(!parameters.getTarget().equalsIgnoreCase(getProductionTargetName()), "clone target cannot be the production environment !!");
		jenkinsClient.startOnClonePipeline(parameters);
	}

	@Override
	public void checkPatchConflicts(List<PatchListParameter> parameters) {
		PatchConflictsChecker conflictsChecker = conflictsCheckerFactory.create();
		parameters.forEach(p -> {
			conflictsChecker.addPatch(repo.findById(p.getPatchNumber()));
		});
		List<PatchConflict> patchConflicts = conflictsChecker.checkConflicts();

		if(!patchConflicts.isEmpty()) {
			jenkinsClient.startNotificationForPatchConflictPipeline(parameters,patchConflicts);
		}
		else {
			LOGGER.info("No conflict found for following patches: " + parameters.stream().map(PatchListParameter::getPatchNumber).collect(Collectors.joining(",")));
		}

	}

	private String getProductionTargetName() {
		String targetName = "chpi211"; // Default
		for(StageMapping stage : repo.stageMappings().getStageMappings()) {
			if(stage.getName().equalsIgnoreCase("production")) {
				targetName = stage.getTarget();
			}
		}
		return targetName;
	}

	@Override
	public void startAssembleAndDeployPipeline(AssembleAndDeployParameters parameters) {
		LOGGER.info("Starting assemble and deploy Pipeline with following parameter: " + parameters.toString());
		if(!parameters.getPatchNumbers().isEmpty()) {
			jenkinsClient.startAssembleAndDeployPipeline(parameters);
		}
		else {
			LOGGER.warn("An assembleAndDeploy Pipeline job was requested without any Patch in the list. Parameters were: " + parameters.toString());
			LOGGER.warn("No assembleAndDeploy Pipeline will be started !");
		}
	}

	@Override
	public void startInstallPipeline(InstallParameters parameters) {
		LOGGER.info("Starting an install Pipeline with following parameter: " + parameters.toString());
		if(!parameters.getPatchNumbers().isEmpty()) {
			jenkinsClient.startInstallPipeline(parameters);
		}
		else {
			LOGGER.warn("An install Pipeline job was requested without any Patch in the list. Parameters were: " + parameters.toString());
			LOGGER.warn("No install Pipeline will be started !");
		}

	}


}
