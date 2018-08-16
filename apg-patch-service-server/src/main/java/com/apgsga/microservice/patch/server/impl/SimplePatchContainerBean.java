package com.apgsga.microservice.patch.server.impl;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
import org.springframework.stereotype.Component;

import com.apgsga.artifact.query.ArtifactDependencyResolver;
import com.apgsga.artifact.query.ArtifactManager;
import com.apgsga.microservice.patch.api.DbModules;
import com.apgsga.microservice.patch.api.DbObject;
import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchOpService;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.api.PatchService;
import com.apgsga.microservice.patch.api.ServiceMetaData;
import com.apgsga.microservice.patch.api.impl.DbObjectBean;
import com.apgsga.microservice.patch.exceptions.Asserts;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.apgsga.microservice.patch.server.impl.jenkins.JenkinsClient;
import com.apgsga.microservice.patch.server.impl.targets.InstallTargetsUtil;
import com.apgsga.microservice.patch.server.impl.vcs.PatchVcsCommand;
import com.apgsga.microservice.patch.server.impl.vcs.VcsCommand;
import com.apgsga.microservice.patch.server.impl.vcs.VcsCommandRunner;
import com.apgsga.microservice.patch.server.impl.vcs.VcsCommandRunnerFactory;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Component("ServerBean")
public class SimplePatchContainerBean implements PatchService, PatchOpService {

	protected final Log LOGGER = LogFactory.getLog(getClass());

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
	public List<MavenArtifact> listMavenArtifacts(Patch patch) {
		ServiceMetaData data = repo.findServiceByName(patch.getServiceName());
		List<MavenArtifact> mavenArtFromStarterList = null;
		try {
			mavenArtFromStarterList = am
					.getAllDependencies(data.getBaseVersionNumber() + "." + data.getRevisionMnemoPart() + "-SNAPSHOT");
		} catch (DependencyResolutionException | ArtifactResolutionException | IOException | XmlPullParserException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException(
					"SimplePatchContainerBean.listMavenArtifacts.exception",
					new Object[] { e.getMessage(), patch.toString() }, e);
		}

		return mavenArtFromStarterList;
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
		Asserts.notNullOrEmpty(patch.getPatchNummer(), "SimplePatchContainerBean.save.patchnumber.notnullorempty.assert",
				new Object[] { patch.toString() });
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
				.forEach(art -> addModuleName(art, patch.getMicroServiceBranch()));
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
		} catch (DependencyResolutionException | ArtifactResolutionException | IOException | XmlPullParserException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException(
					"SimplePatchContainerBean.addModuleName.exception",
					new Object[] { e.getMessage(), art.toString(), cvsBranch }, e);

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

	private List<MavenArtifact> getArtifactsWithNameFromBom(String version) {
		List<MavenArtifact> artifactsWithNameFromBom = null;
		try {
			artifactsWithNameFromBom = am.getArtifactsWithNameFromBom(version);
		} catch (Exception e) {
			throw ExceptionFactory.createPatchServiceRuntimeException(
					"SimplePatchContainerBean.getArtifactsWithNameFromBom.exception",
					new Object[] { e.getMessage(), version }, e);
		}

		return artifactsWithNameFromBom;
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
	public List<MavenArtifact> invalidArtifactNames(String version, String cvsBranch) {
		return getArtifactNameError(getArtifactsWithNameFromBom(version), cvsBranch);
	}
	
	@Override
	public void onClone(String target) {
		jenkinsClient.onClone(target);
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
}
