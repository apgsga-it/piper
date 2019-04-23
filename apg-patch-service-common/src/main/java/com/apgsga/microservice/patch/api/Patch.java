package com.apgsga.microservice.patch.api;

import java.util.List;

import com.affichage.persistence.common.client.EntityRootInterface;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@EntityRootInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
public interface Patch extends ServiceMetaData {

	public static final String PATCH_NUMMER = "patchNummer";

	public static final String SERVICE_NAME = "serviceName";

	public static final String MICROSERVICE_BRANCH = "microServiceBranch";

	public static final String DB_PATCH_BRANCH = "dbPatchBranch";

	public static final String PROD_BRANCH = "prodBranch";

	public static final String DB_OBJECTS = "dbObjects";

	public static final String MAVEN_ARTEFACTS = "mavenArtifacts";

	public static final String TAG_NR = "tagNr";

	public static final String PATCH_TAG = "patchTag";
	
	public static final String DEVELOPER_BRANCH = "developerBranch";

	public static final String INSTALLS_TARGET = "installationTarget";
	
	public static final String CURRENT_TARGET = "currentTarget";
	
	public static final String INSTALL_ON_EMPTY_MODULES = "installOnEmptyModules";
	
	public static final String INSTALL_JADAS_AND_GUI = "installJadasAndGui";
	
	public static final String PIPELINE_TASK = "pipeLineTask";
	
	public static final String STEP = "step";

	String getPatchNummer();

	void setPatchNummer(String patchNummer);

	String getMicroServiceBranch();

	void setMicroServiceBranch(String microServiceBranch);

	String getDbPatchBranch();

	void setDbPatchBranch(String dbPatchBranch);

	String getProdBranch();

	void setProdBranch(String prodBranch);

	void setServiceVersion(ServiceMetaData serviceVersion);

	String getServiceName();

	public String getBaseVersionNumber();

	public String getRevisionMnemoPart();

	List<DbObject> getDbObjects();

	List<String> getDbObjectsAsVcsPath();

	void setDbObjects(List<DbObject> dbObjects);

	void removeDbObjects(DbObject dbObject);

	void addDbObjects(DbObject dbObject);

	List<MavenArtifact> getMavenArtifacts();

	List<String> getMavenArtifactsAsVcsPath();
	
	List<MavenArtifact> getMavenArtifactsToBuild();

	void setMavenArtifacts(List<MavenArtifact> mavenArtifacts);

	void removeMavenArtifacts(MavenArtifact mavenArtifact);

	void addMavenArtifacts(MavenArtifact mavenArtifact);

	public Integer getTagNr();

	void setTagNr(Integer tagNr);

	public void incrementTagNr();

	public String getPatchTag();

	void setPatchTag(String patchTag);
	
	public String getDeveloperBranch();

	void setDeveloperBranch(String developerBranch);

	public String getInstallationTarget();

	public void setInstallationTarget(String target);

	public String getRevision();

	public void setRevision(String revisionNumber);

	public String getLastRevision();

	public void setLastRevision(String lastRevisionNumber);
	
	public String getRunningNr();

	public void setRunningNr(String runningNr);
	
	public String getTargetToState(); 
	
	public void setTargetToState(String targetToState);
	
	public String getLastPipelineTask(); 
	
	public void setLastPipelineTask(String pipelineTask);
	
	public boolean getInstallOnEmptyModules();
	
	public void setInstallOnEmptyModules(boolean installOnEmptyModules);
	
	public boolean getInstallJadasAndGui();
	
	public void setInstallJadasAndGui();
	
	public String getCurrentTarget();
	
	public void setCurrentTarget(String currentTarget);
	
	public String getStep();
	
	public void setStep(String step);

}