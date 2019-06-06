package com.apgsga.microservice.patch.proposal2.api.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.affichage.persistence.common.client.AbstractTransientEntity;
import com.apgsga.microservice.patch.api.DbObject;
import com.apgsga.microservice.patch.proposal2.api.Patch;
import com.apgsga.microservice.patch.proposal2.api.Service;
import com.google.common.collect.Lists;

public class PatchBean extends AbstractTransientEntity implements Patch {

	private static final String PROD_BRANCH_DEFAULT = "HEAD";
	private static final long serialVersionUID = 1L;

	private String patchNummer;
	private String dbPatchBranch;
	private String prodBranch = PROD_BRANCH_DEFAULT;
	private String patchTag = "";
	private String developerBranch = "";
	private Integer tagNr = 0;
	private String installationTarget;
	private String targetToState;
	private String revision;
	private String lastRevision;
	private String runningNr; 
	private List<DbObject> dbObjects = Lists.newArrayList();
	private List<Service> services = Lists.newArrayList();
	private boolean installOnEmptyModules = false;
	private String lastPipelineTask ="";
	private String currentTarget;
	private String currentPipelineTask;
	private String logText;

	public PatchBean() {
		super();
	}

	@Override
	public String getPatchNummer() {
		return patchNummer;
	}

	@Override
	public void setPatchNummer(String patchNummer) {
		final Object oldValue = this.patchNummer;
		this.patchNummer = patchNummer;
		firePropertyChangeEvent(PATCH_NUMMER, oldValue, patchNummer);
	}


	@Override
	public String getDbPatchBranch() {
		return dbPatchBranch;
	}

	@Override
	public void setDbPatchBranch(String dbPatchBranch) {
		final Object oldValue = this.dbPatchBranch;
		this.dbPatchBranch = dbPatchBranch;
		firePropertyChangeAndMarkDirty(DB_PATCH_BRANCH, oldValue, dbPatchBranch);
	}

	@Override
	public String getProdBranch() {
		return prodBranch;
	}

	@Override
	public void setProdBranch(String prodBranch) {
		final Object oldValue = this.prodBranch;
		this.prodBranch = prodBranch;
		firePropertyChangeAndMarkDirty(PROD_BRANCH, oldValue, prodBranch);
	}
	
	@Override
	public String getDeveloperBranch() {
		return developerBranch;
	}

	@Override
	public void setDeveloperBranch(String developerBranch) {
		final Object oldValue = this.developerBranch;
		this.developerBranch = developerBranch;
		firePropertyChangeAndMarkDirty(DEVELOPER_BRANCH, oldValue, developerBranch);
	}

	@Override
	public List<DbObject> getDbObjects() {
		return dbObjects;
	}

	@Override
	public List<String> getDbObjectsAsVcsPath() {
		return dbObjects.stream().map(dbObjects -> dbObjects.asFullPath()).collect(Collectors.toList());
	}

	@Override
	public void setDbObjects(List<DbObject> dbObjects) {
		final Object oldValue = Lists.newArrayList(this.dbObjects);
		this.dbObjects = dbObjects;
		firePropertyChangeEvent(DB_OBJECTS, oldValue, dbObjects);
	}

	@Override
	public void removeDbObjects(DbObject dbObject) {
		final Object oldValue = Lists.newArrayList(this.dbObjects);
		dbObjects.remove(dbObject);
		firePropertyChangeAndMarkDirty(DB_OBJECTS, oldValue, dbObjects);
	}

	@Override
	public void addDbObjects(DbObject dbObject) {
		final Object oldValue = Lists.newArrayList(this.dbObjects);
		dbObjects.add(dbObject);
		firePropertyChangeAndMarkDirty(DB_OBJECTS, oldValue, dbObjects);
	}

	

	@Override
	public void incrementTagNr() {
		Integer tagNr = getTagNr();
		setTagNr(++tagNr);
	}

	@Override
	public Integer getTagNr() {
		return tagNr;
	}

	@Override
	public void setTagNr(Integer tagNr) {
		final Object oldValue = this.tagNr;
		this.tagNr = tagNr;
		firePropertyChangeEvent(TAG_NR, oldValue, tagNr);
	}

	@Override
	public String getPatchTag() {
		return patchTag;
	}

	@Override
	public void setPatchTag(String patchTag) {
		final Object oldValue = this.patchTag;
		this.patchTag = patchTag;
		firePropertyChangeEvent(PATCH_TAG, oldValue, patchTag);
	}

	@Override
	public String getInstallationTarget() {
		return installationTarget;
	}

	@Override
	public void setInstallationTarget(String installationTarget) {
		final Object oldValue = this.installationTarget;
		this.installationTarget = installationTarget;
		firePropertyChangeEvent(INSTALLS_TARGET, oldValue, installationTarget);
	}
	
	@Override
	public String getCurrentTarget() {
		return currentTarget;
	}

	@Override
	public void setCurrentTarget(String currentTarget) {
		final Object oldValue = this.currentTarget;
		this.currentTarget = currentTarget;
		firePropertyChangeEvent(CURRENT_TARGET, oldValue, currentTarget);
	}

	@Override
	public String getLogText() {
		return logText;
	}

	@Override
	public void setLogText(String logText) {
		final Object oldValue = this.logText;
		this.logText = logText;
		firePropertyChange(LOG_TEXT, oldValue, logText);
	}

	@Override
	public String getCurrentPipelineTask() {
		return currentPipelineTask;
	}

	@Override
	public void setCurrentPipelineTask(String currentPipelineTask) {
		final Object oldValue = this.currentPipelineTask;
		this.currentPipelineTask = currentPipelineTask;
		firePropertyChange(CURRENT_PIPELINE_TASK, oldValue, currentPipelineTask);
	}


	@Override
	public String getRevision() {
		return revision;
	}

	@Override
	public void setRevision(String revisionNumber) {
		this.revision = revisionNumber;
	}

	@Override
	public String getLastRevision() {
		return lastRevision;
	}

	@Override
	public void setLastRevision(String lastRevisionNumber) {
		this.lastRevision = lastRevisionNumber;
	}
	
	@Override
	public boolean getInstallOnEmptyModules() {
		return installOnEmptyModules;
	}

	@Override
	public void setInstallOnEmptyModules(boolean installOnEmptymodules) {
		final Object oldValue = this.installOnEmptyModules;
		this.installOnEmptyModules = installOnEmptymodules;
		firePropertyChangeEvent(INSTALL_ON_EMPTY_MODULES, oldValue, installOnEmptyModules);
	}
	
	@Override
	public boolean getInstallJadasAndGui() {
	//	return !getMavenArtifacts().isEmpty() || installOnEmptyModules;
		return false;
	}

	@Override
	public void setInstallJadasAndGui() {
		// Intentionally empty
	}
	

	@Override
	public String getRunningNr() {
		return this.runningNr;
	}

	@Override
	public void setRunningNr(String runningNr) {
		this.runningNr = runningNr; 
	}
	
	
	@Override
	public String getTargetToState() {
		return targetToState;
	}

	@Override
	public void setTargetToState(String targetToState) {
		this.targetToState = targetToState;
	}
	
	
	@Override
	public String getLastPipelineTask() {
		return lastPipelineTask;
	}

	@Override
	public void setLastPipelineTask(String pipelineTask) {
		this.lastPipelineTask = pipelineTask;
	}

	@Override
	public List<Service> getServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setServices(List<Service> services) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeService(Service service) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addService(Service service) {
		// TODO Auto-generated method stub
		
	}

	
	
}
