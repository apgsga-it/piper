package com.apgsga.microservice.patch.core.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.apgsga.microservice.patch.api.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.apgsga.artifact.query.ArtifactDependencyResolver;
import com.apgsga.artifact.query.ArtifactManager;
import com.apgsga.microservice.patch.exceptions.Asserts;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.apgsga.microservice.patch.core.impl.jenkins.JenkinsClient;
import com.apgsga.microservice.patch.core.impl.targets.InstallTargetsUtil;
import com.apgsga.microservice.patch.core.impl.vcs.PatchVcsCommand;
import com.apgsga.microservice.patch.core.impl.vcs.VcsCommand;
import com.apgsga.microservice.patch.core.impl.vcs.VcsCommandRunner;
import com.apgsga.microservice.patch.core.impl.vcs.VcsCommandRunnerFactory;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.offbytwo.jenkins.model.BuildResult;

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
	private ArtifactDependencyResolver dependecyResolver;

	@Autowired
	private VcsCommandRunnerFactory vcsCommandRunnerFactory;

	@Autowired
	private PatchActionExecutorFactory patchActionExecutorFactory;

	@Autowired
	private TaskExecutor threadExecutor;

	@Value("${config.common.location:/etc/opt/apg-patch-common}")
	private String configCommon;

	@Value("${config.common.targetSystemFile:TargetSystemMappings.json}")
	private String targetSystemFile;

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
		ServiceMetaData data = repo.findServiceByName(serviceName);
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
		Asserts.notNullOrEmpty(patch.getPatchNummer(),
				"SimplePatchContainerBean.save.patchnumber.notnullorempty.assert", new Object[] { patch.toString() });
		preProcessSave(patch);
		repo.savePatch(patch);
		return patch;
	}
	
	@Override
	public void log(Patch patch) {
		//JHE: To be verified, any other pre-check to be done? Eventually that patchLog.patchLogDetail is correct
		Asserts.notNull(patch, "SimplePatchContainerBean.log.patch.null.assert", new Object[] {});
		Asserts.notNullOrEmpty(patch.getPatchNummer(), "SimplePatchContainerBean.log.patchnumber.isnullorempty", new Object[] {});
		Asserts.notNull(repo.findById(patch.getPatchNummer()), "SimplePatchContainerBean.log.patchisnull", new Object[] {patch.getPatchNummer()});
		repo.savePatchLog(patch);
	}

	private void preProcessSave(Patch patch) {
		if (!repo.patchExists(patch.getPatchNummer())) {
			createBranchForDbModules(patch);
			jenkinsClient.createPatchPipelines(patch);
		}
		// TODO (MULTISERVICE_CM , 9.4) : Needs to be verified
		for (Service service : patch.getServices()) {
			service.getMavenArtifactsToBuild().stream().filter(art -> Strings.isNullOrEmpty(art.getName()))
					.forEach(art -> addModuleName(art, service.getMicroServiceBranch()));
		}

	}

	private MavenArtifact addModuleName(MavenArtifact art, String cvsBranch) {
		try {
			String artifactName = am.getArtifactName(art.getGroupId(), art.getArtifactId(), art.getVersion());

			List<MavenArtifact> wrongNames = getArtifactNameError(Lists.newArrayList(art), cvsBranch);
			if (!wrongNames.isEmpty()) {
				throw ExceptionFactory.createPatchServiceRuntimeException(
						"SimplePatchContainerBean.addModuleName.validation.error", new Object[] { art.toString() });
			}

			art.setName(artifactName);
		} catch (Exception e) {
			throw ExceptionFactory.createPatchServiceRuntimeException(
					"SimplePatchContainerBean.addModuleName.exception",
					new Object[]{e.getMessage(), art.toString(), cvsBranch}, e);
		}
		return art;
	}

	@Override
	public void remove(Patch patch) {
		repo.removePatch(patch);
	}

	private void createBranchForDbModules(Patch patch) {
		DbModules dbModules = repo.getDbModules();
		if (dbModules == null) {
			return;
		}
		final VcsCommandRunner vcsCommandRunner = vcsCommandRunnerFactory.create();
		vcsCommandRunner.preProcess();
		vcsCommandRunner.run(PatchVcsCommand.createCreatePatchBranchCmd(patch.getDbPatchBranch(), patch.getProdBranch(),
				dbModules.getDbModules()));
		vcsCommandRunner.postProcess();

	}

	@Override
	public List<String> listInstallationTargetsFor(String requestingTarget) {
		ResourceLoader rl = new FileSystemResourceLoader();
		Resource targetConfigFile = rl.getResource(configCommon + "/" + targetSystemFile);
		return InstallTargetsUtil.listInstallTargets(targetConfigFile);
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
		final VcsCommandRunner vcsCmdRunner = vcsCommandRunnerFactory.create();
		vcsCmdRunner.preProcess();
		List<DbObject> dbObjects = Lists.newArrayList();
		for (String dbModule : dbModules.getDbModules()) {
			if (Strings.isNullOrEmpty(dbModule) || dbModule.contains(searchString)) {
				List<String> result = vcsCmdRunner.run(PatchVcsCommand
						.createDiffPatchModulesCmd(patch.getDbPatchBranch(), patch.getProdBranch(), dbModule));
				List<String> files = result.stream()
						.filter(s -> s.startsWith("Index: "))
						.map(s -> s.substring(7)).collect(Collectors.toList());
				files.stream().forEach(file -> {
					DbObject dbObject = new DbObject();
					dbObject.setModuleName(dbModule);
					dbObject.setFileName(FilenameUtils.getName(file));
					dbObject.setFilePath(FilenameUtils.getPath(file));
					dbObjects.add(dbObject);
				});

			}
		}
		vcsCmdRunner.postProcess();
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
		final VcsCommandRunner vcsCmdRunner = vcsCommandRunnerFactory.create();
		vcsCmdRunner.preProcess();
		List<DbObject> dbObjects = Lists.newArrayList();
		for (String dbModule : dbModules.getDbModules()) {
			if (dbModule.contains(searchString)) {
				String tempSubFolderName= "apg_patch_ui_temp_";
				String tmpDir = System.getProperty("java.io.tmpdir");
				String coFolder = tmpDir + "/" + tempSubFolderName + suffixForCoFolder;
				String additionalOptions = "-d " + coFolder;
				LOGGER.info("Temporary checkout folder for listing all DB Objects will be: " + coFolder);
				List<String> result = vcsCmdRunner.run(PatchVcsCommand.createCoCvsModuleToDirectoryCmd(patch.getDbPatchBranch(), patch.getProdBranch(), Lists.newArrayList(dbModule), additionalOptions));
				result.forEach(r -> {
					// JHE : In production, cvs is on a separated server, therefore we can't checkout, and parse the local result ...
					//		 We rely on the output given back from the CVS command, might not be the most robust solution :( ... but so far ok for a function which is not crucial.
					int startIndex = r.indexOf("U ")+"U ".length();
					String pathToResourceName = r.substring(startIndex, r.length()).trim().replaceFirst(suffixForCoFolder, "").replaceFirst(tmpDir + "/", "");
					DbObject dbObject = new DbObject();
					dbObject.setModuleName(dbModule);
					dbObject.setFileName(FilenameUtils.getName(pathToResourceName));
					dbObject.setFilePath(dbModule + "/" + FilenameUtils.getPath(pathToResourceName.replaceFirst(tempSubFolderName,"")));
					dbObjects.add(dbObject);
				});

				List<String> rmResult = vcsCmdRunner.run(PatchVcsCommand.createRmTmpCheckoutFolder(coFolder));
			}
		}
		vcsCmdRunner.postProcess();
		return dbObjects;
	}

	@Override
	public synchronized void startInstallPipeline(Patch patch) {
		Asserts.notNull(patch, "SimplePatchContainerBean.startInstallPipeline.patchobject.notnull.assert",
				new Object[] {});
		Asserts.notNull(patch.getPatchNummer(),
				"SimplePatchContainerBean.startInstallPipeline.patchnumber.notnull.assert",
				new Object[] { patch.toString() });
		Asserts.isTrue((repo.patchExists(patch.getPatchNummer())),
				"SimplePatchContainerBean.startInstallPipeline.patch.exists.assert", new Object[] { patch.toString() });
		repo.savePatch(patch);
		jenkinsClient.startInstallPipeline(patch);
	}

	@Override
	public void executeStateTransitionAction(String patchNumber, String toStatus) {
		PatchActionExecutor patchActionExecutor = patchActionExecutorFactory.create(this);
		patchActionExecutor.execute(patchNumber, toStatus);
	}

	@Override
	public void restartProdPipeline(String patchNumber) {
		Asserts.notNull(patchNumber, "SimplePatchContainerBean.restartProdPipeline.patchnumber.notnull.assert",
				new Object[] {});
		Asserts.isTrue((repo.patchExists(patchNumber)),
				"SimplePatchContainerBean.restartProdPipeline.patch.exists.assert", new Object[] { patchNumber });
		Asserts.isFalse(jenkinsClient.isProdPatchPipelineRunning(patchNumber), "SimplePatchContainerBean.restartProdPipeline.patch.alreadyRunning", new Object[]{patchNumber});
		Asserts.isTrue(isLastProdPipelineAbortedOrInError(patchNumber), "SimplePatchContainerBean.restartProdPipeline.patch.lastBuildInErrorOrAborted", new Object[]{patchNumber});
		Patch patch = repo.findById(patchNumber);
		jenkinsClient.restartProdPatchPipeline(patch);
	}
	
	private boolean isLastProdPipelineAbortedOrInError(String patchNumber) {
		BuildResult result = jenkinsClient.getProdPipelineBuildResult(patchNumber);
		return result.equals(BuildResult.ABORTED) || result.equals(BuildResult.FAILURE);
	}
	
	private List<MavenArtifact> getArtifactNameError(List<MavenArtifact> mavenArtifacts, String cvsBranch) {

		VcsCommandRunner cmdRunner = getJschSessionFactory().create();
		cmdRunner.preProcess();
		List<MavenArtifact> artifactWihInvalidNames = Lists.newArrayList();

		for (MavenArtifact ma : mavenArtifacts) {
			try {
				String artifactName = am.getArtifactName(ma.getGroupId(), ma.getArtifactId(), ma.getVersion());
				ma.setName(artifactName);

				if (artifactName == null) {
					artifactWihInvalidNames.add(ma);
				} else {
					VcsCommand silentCoCmd = PatchVcsCommand.createSilentCoCvsModuleCmd(cvsBranch,
							Lists.newArrayList(artifactName), "&>/dev/null ; echo $?");
					List<String> cvsResults = cmdRunner.run(silentCoCmd);
					// JHE: SilentCOCvsModuleCmd returns 0 when all OK, 1
					// instead...
					if (cvsResults.size() != 1 || cvsResults.get(0).equals("1")) {
						artifactWihInvalidNames.add(ma);
					}
				}
			} catch (Exception e) {
				throw ExceptionFactory.createPatchServiceRuntimeException(
						"SimplePatchContainerBean.getArtifactNameError.exception",
						new Object[] { e.getMessage(), ma.toString() }, e);
			}
		}

		cmdRunner.postProcess();
		return artifactWihInvalidNames;
	}

	@Override
	public void cleanLocalMavenRepo() {
		am.cleanLocalMavenRepo();

	}

	@Override
	public void onClone(String source, String target) {
		jenkinsClient.onClone(source,target);
	}

	public ArtifactDependencyResolver getDependecyResolver() {
		return dependecyResolver;
	}

	public PatchPersistence getRepo() {
		return repo;
	}

	protected void setRepo(PatchPersistence repo) {
		this.repo = repo;
	}

	public JenkinsClient getJenkinsClient() {
		return jenkinsClient;
	}

	protected void setJenkinsClient(JenkinsClient jenkinsClient) {
		this.jenkinsClient = jenkinsClient;
	}

	public VcsCommandRunnerFactory getJschSessionFactory() {
		return vcsCommandRunnerFactory;
	}

	public TaskExecutor getThreadExecutor() {
		return threadExecutor;
	}

	@Override
	public List<Patch> findWithObjectName(String objectName) {
		List<String> patchFiles = repo.listFiles("Patch");
		// Filter out "PatchLog" files, and extract Id from patch name
		List<String> patchFilesReduced = patchFiles.stream().filter(pat -> !pat.contains("PatchLog")).map(pat -> pat.substring(pat.indexOf("Patch")+"Patch".length(),pat.indexOf(".json"))).collect(Collectors.toList());
		return patchFilesReduced.stream().filter(p -> containsObject(p,objectName)).map(this::findById).collect(Collectors.toList());
	}

	@Override
	public void startAssembleAndDeployPipeline(String target) {
		jenkinsClient.startAssembleAndDeployPipeline(target);
	}

	private boolean containsObject(String patchNumber, String objectName) {
		Patch patch = findById(patchNumber);
		for(MavenArtifact ma : patch.getMavenArtifacts()) {
			// TODO JHE : verifiy if we really want to check only on artifact id, maybe also on name?
			if(ma.getArtifactId()!= null && ma.getArtifactId().toUpperCase().contains(objectName.toUpperCase()))
				return true;
		}
		for(DbObject dbo : patch.getDbObjects()) {
			// TODO JHE : verifiy if we really want to check on fileName
			if(dbo.getFileName() != null && dbo.getFileName().toUpperCase().contains(objectName.toUpperCase())) {
				return true;
			}
		}
		return false;
	}
}
