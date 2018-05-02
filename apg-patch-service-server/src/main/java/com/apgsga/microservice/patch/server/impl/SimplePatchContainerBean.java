package com.apgsga.microservice.patch.server.impl;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.apgsga.artifact.query.ArtifactManager;
import com.apgsga.microservice.patch.api.DbModules;
import com.apgsga.microservice.patch.api.DbObject;
import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchOpService;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.api.PatchService;
import com.apgsga.microservice.patch.api.ServiceMetaData;
import com.apgsga.microservice.patch.api.TargetSystemEnviroment;
import com.apgsga.microservice.patch.api.impl.DbObjectBean;
import com.apgsga.microservice.patch.server.impl.jenkins.JenkinsPatchClient;
import com.apgsga.microservice.patch.server.impl.vcs.PatchVcsCommand;
import com.apgsga.microservice.patch.server.impl.vcs.VcsCommand;
import com.apgsga.microservice.patch.server.impl.vcs.VcsCommandRunner;
import com.apgsga.microservice.patch.server.impl.vcs.VcsCommandRunnerFactory;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Component("ServerBean")
public class SimplePatchContainerBean implements PatchService, PatchOpService {

	protected final Log LOGGER = LogFactory.getLog(getClass());
	
	@Autowired
	@Qualifier("patchPersistence")
	private PatchPersistence repo;

	@Autowired
	private JenkinsPatchClient jenkinsClient;

	@Autowired
	private ArtifactManager am;

	@Autowired
	private VcsCommandRunnerFactory vcsCommandRunnerFactory;
	
	@Autowired
	private PatchActionExecutorFactory patchActionExecutorFactory; 

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
	public List<MavenArtifact> listMavenArtifacts(Patch patch) {
		ServiceMetaData data = repo.findServiceByName(patch.getServiceName());
		List<MavenArtifact> mavenArtFromStarterList = null;
		try {
			mavenArtFromStarterList = am
					.getAllDependencies(data.getBaseVersionNumber() + "." + data.getRevisionMnemoPart() + "-SNAPSHOT");
		} catch (DependencyResolutionException | ArtifactResolutionException | IOException | XmlPullParserException e) {

			new RuntimeException(e);
		}

		return mavenArtFromStarterList;
	}

	@Override
	public List<ServiceMetaData> listServiceData() {
		return repo.getServicesMetaData().getServicesMetaData();
	}

	@Override
	public Patch findById(String patchNummer) {
		Preconditions.checkNotNull(patchNummer, "No Patchnummer");
		return repo.findById(patchNummer);
	}

	@Override
	public Patch save(Patch patch) {
		Preconditions.checkNotNull(patch.getPatchNummer(), "Patchnummer null");
		preProcessSave(patch);
		repo.savePatch(patch);
		return patch;
	}

	private void preProcessSave(Patch patch) {
		if (!repo.patchExists(patch.getPatchNummer())) {
			createBranchForDbModules(patch);
			jenkinsClient.createPatchPipelines(patch);
		}
		patch.getMavenArtifacts().stream().filter(art -> Strings.isNullOrEmpty(art.getName()))
				.forEach(art -> addModuleName(art,patch.getMicroServiceBranch()));
	}

	private MavenArtifact addModuleName(MavenArtifact art, String cvsBranch) {
		try {
			String artifactName = am.getArtifactName(art.getGroupId(), art.getArtifactId(), art.getVersion());
			
			List<MavenArtifact> wrongNames = getArtifactNameError(Lists.newArrayList(art),cvsBranch);
			if(!wrongNames.isEmpty()) {
				throw new RuntimeException("Patch cannot be saved as it contains module(s) with invalid name: " + wrongNames);
			}
			
			art.setName(artifactName);
		} catch (DependencyResolutionException | ArtifactResolutionException | IOException | XmlPullParserException e) {
			new RuntimeException(e);
		}
		return art;
	}

	@Override
	public void remove(Patch patch) {
		Preconditions.checkNotNull(patch.getPatchNummer(), "Patchnummer null");
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
		// TODO (che, 13.12) : Filtering Rules per requestTarget
		return repo.getInstallationTargets().stream().map(e -> e.getName()).collect(Collectors.toList());
	}

	@Override
	public List<DbObject> listAllObjectsChangedForDbModule(String patchId, String searchString) {
		Patch patch = findById(patchId);
		if (patch == null) {
			throw new RuntimeException("Patch with Id: " + patchId + " not found");
		}
		DbModules dbModules = repo.getDbModules();
		if (dbModules == null) {
			return Lists.newArrayList();
		}
		final VcsCommandRunner vcsCmdRunner = vcsCommandRunnerFactory.create();
		vcsCmdRunner.preProcess();
		List<DbObject> dbObjects = Lists.newArrayList();
		for (String dbModule : dbModules.getDbModules()) {
			if (Strings.isNullOrEmpty(dbModule) || dbModule.contains(searchString)) {
				List<String> result = vcsCmdRunner.run(
						PatchVcsCommand.createDiffPatchModulesCmd(patch.getDbPatchBranch(), patch.getProdBranch(), dbModule));
				List<String> files = result.stream()
						.filter(s -> s.startsWith("Index: ") && (s.endsWith("sql") || s.endsWith("deleted")))
						.map(s -> s.substring(7)).collect(Collectors.toList());
				files.stream().forEach(file -> {
					DbObject dbObject = new DbObjectBean();
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
	public synchronized void startInstallPipeline(Patch patch) {
		repo.savePatch(patch);
		String installationTarget = patch.getInstallationTarget();
		TargetSystemEnviroment installationTargetData = repo.getInstallationTarget(installationTarget);
		Assert.notNull(installationTargetData, "Installationtarget : " + installationTarget + " not found");
		jenkinsClient.startInstallPipeline(patch);
	}

	@Override
	public void executeStateTransitionAction(String patchNumber, String toStatus) {
		PatchActionExecutor patchActionExecutor = patchActionExecutorFactory.create(this);
		patchActionExecutor.execute(patchNumber, toStatus);
	}

	public PatchPersistence getRepo() {
		return repo;
	}

	protected void setRepo(PatchPersistence repo) {
		this.repo = repo;
	}

	public JenkinsPatchClient getJenkinsClient() {
		return jenkinsClient;
	}

	protected void setJenkinsClient(JenkinsPatchClient jenkinsClient) {
		this.jenkinsClient = jenkinsClient;
	}

	public VcsCommandRunnerFactory getJschSessionFactory() {
		return vcsCommandRunnerFactory;
	}
	
	private List<MavenArtifact> getArtifactsWithNameFromBom(String version) {
		List<MavenArtifact> artifactsWithNameFromBom = null;
		try {
			artifactsWithNameFromBom = am.getArtifactsWithNameFromBom(version);
		}
		catch(Exception ex) {
			LOGGER.error("Error when trying to get Artifact name from Bom for version " + version);
			LOGGER.error(ex.getMessage());
			LOGGER.error(ExceptionUtils.getStackTrace(ex));
			throw new RuntimeException("Error when trying to get Artifact name from Bom for version " + version);
		}
		
		return artifactsWithNameFromBom;
	}
	
	private List<MavenArtifact> getArtifactNameError(List<MavenArtifact> mavenArtifacts, String cvsBranch) {
		
		VcsCommandRunner cmdRunner = getJschSessionFactory().create();
		cmdRunner.preProcess();
		List<MavenArtifact> artifactWihInvalidNames = Lists.newArrayList();
		
		for(MavenArtifact ma : mavenArtifacts) {
			try {
				String artifactName = am.getArtifactName(ma.getGroupId(), ma.getArtifactId(), ma.getVersion());
				ma.setName(artifactName);
				
				if(artifactName == null) {
					artifactWihInvalidNames.add(ma);
				}
				else {
					VcsCommand silentCoCmd = PatchVcsCommand.createSilentCoCvsModuleCmd(cvsBranch, Lists.newArrayList(artifactName),"&>/dev/null ; echo $?");
					List<String> cvsResults = cmdRunner.run(silentCoCmd);
					// JHE: SilentCOCvsModuleCmd returns 0 when all OK, 1 instead...
					if(cvsResults.size() != 1 || cvsResults.get(0).equals("1")) {
						artifactWihInvalidNames.add(ma);
					}
				}
			}
			catch(Exception ex) {
				LOGGER.error("Error when trying to list Artifact having error in their names.");
				LOGGER.error(ex.getMessage());
				LOGGER.error(ExceptionUtils.getStackTrace(ex));
				throw new RuntimeException("Error when trying to list Artifact having error in their names");
			}
		}
		
		cmdRunner.postProcess();
		return artifactWihInvalidNames;
	}
	
	@Override
	public List<MavenArtifact> invalidArtifactNames(String version, String cvsBranch) {
		return getArtifactNameError(getArtifactsWithNameFromBom(version),cvsBranch);
	}
}
