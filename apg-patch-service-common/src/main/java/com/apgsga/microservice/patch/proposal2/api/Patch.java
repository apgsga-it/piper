package com.apgsga.microservice.patch.proposal2.api;

import java.util.List;

import com.affichage.persistence.common.client.EntityRootInterface;
import com.apgsga.microservice.patch.api.DbObject;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@EntityRootInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
public interface Patch {

	public static final String PATCH_NUMMER = "patchNummer";

	public static final String MICROSERVICE_BRANCH = "microServiceBranch";

	public static final String DB_PATCH_BRANCH = "dbPatchBranch";

	public static final String PROD_BRANCH = "prodBranch";

	public static final String DB_OBJECTS = "dbObjects";

	public static final String TAG_NR = "tagNr";

	public static final String PATCH_TAG = "patchTag";
	
	public static final String DEVELOPER_BRANCH = "developerBranch";

	public static final String INSTALLS_TARGET = "installationTarget";
	
	public static final String CURRENT_TARGET = "currentTarget";
	
	public static final String INSTALL_ON_EMPTY_MODULES = "installOnEmptyModules";
	
	public static final String INSTALL_JADAS_AND_GUI = "installJadasAndGui";
	
	public static final String PIPELINE_TASK = "pipeLineTask";
	
	public static final String LOG_TEXT = "logText";
	
	public static final String CURRENT_PIPELINE_TASK = "currentPipelineTask";

	String getPatchNummer();

	void setPatchNummer(String patchNummer);

	String getDbPatchBranch();

	void setDbPatchBranch(String dbPatchBranch);

	String getProdBranch();

	void setProdBranch(String prodBranch);

	List<DbObject> getDbObjects();

	List<String> getDbObjectsAsVcsPath();

	void setDbObjects(List<DbObject> dbObjects);

	void removeDbObjects(DbObject dbObject);

	void addDbObjects(DbObject dbObject);
	
	List<Service> getServices();

	void setServices(List<Service> services);

	void removeService(Service service);

	void addService(Service service);
	
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
	
	public String getLogText();
	
	public void setLogText(String logText);
	
	public String getCurrentPipelineTask();
	
	public void setCurrentPipelineTask(String currentPipelineTask);

}