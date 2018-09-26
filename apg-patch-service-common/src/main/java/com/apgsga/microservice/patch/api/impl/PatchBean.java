package com.apgsga.microservice.patch.api.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.affichage.persistence.common.client.AbstractTransientEntity;
import com.apgsga.microservice.patch.api.DbObject;
import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.ServiceMetaData;
import com.google.common.collect.Lists;

public class PatchBean extends AbstractTransientEntity implements Patch {

	private static final String PROD_BRANCH_DEFAULT = "HEAD";
	private static final long serialVersionUID = 1L;

	private String patchNummer;
	private String serviceName = "It21Ui";
	private String microServiceBranch;
	private String dbPatchBranch;
	private String prodBranch = PROD_BRANCH_DEFAULT;
	private String patchTag = "";
	private Integer tagNr = 0;
	private String installationTarget;
	private String baseVersionNumber;
	private String revisionMnemoPart;
	private String revision;
	private String lastRevision;
	private String runningNr; 
	private List<DbObject> dbObjects = Lists.newArrayList();
	private List<MavenArtifact> mavenArtifacts = Lists.newArrayList();
	private boolean installOnEmptyModules = false;

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
	public String getServiceName() {
		return serviceName;
	}

	@Override
	public void setServiceVersion(ServiceMetaData serviceVersion) {
		setServiceName(serviceVersion.getServiceName());
		setBaseVersionNumber(serviceVersion.getBaseVersionNumber());
		setRevisionMnemoPart(serviceVersion.getRevisionMnemoPart());
		setMicroServiceBranch(serviceVersion.getMicroServiceBranch());

	}

	@Override
	public void setServiceName(String serviceName) {
		final Object oldValue = this.serviceName;
		this.serviceName = serviceName;
		firePropertyChangeAndMarkDirty(SERVICE_NAME, oldValue, serviceName);
	}

	@Override
	public String getMicroServiceBranch() {
		return microServiceBranch;
	}

	@Override
	public void setMicroServiceBranch(String microServiceBranch) {
		final Object oldValue = this.microServiceBranch;
		this.microServiceBranch = microServiceBranch;
		firePropertyChangeAndMarkDirty(MICROSERVICE_BRANCH, oldValue, microServiceBranch);
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
	public List<MavenArtifact> getMavenArtifacts() {
		return mavenArtifacts;
	}

	@Override
	public List<String> getMavenArtifactsAsVcsPath() {
		return getMavenArtifactsToBuild().stream().map(mavenArt -> mavenArt.getName()).collect(Collectors.toList());
	}
	
	@Override
	public List<MavenArtifact> getMavenArtifactsToBuild() {
		return mavenArtifacts.stream().filter(m -> m.getVersion().endsWith("SNAPSHOT")).collect(Collectors.toList()); 
	}

	@Override
	public void setMavenArtifacts(List<MavenArtifact> mavenArtifacts) {
		final Object oldValue = Lists.newArrayList(this.dbObjects);
		this.mavenArtifacts = mavenArtifacts;
		firePropertyChangeEvent(MAVEN_ARTEFACTS, oldValue, mavenArtifacts);
		
	}

	@Override
	public void removeMavenArtifacts(MavenArtifact mavenArtifact) {
		final Object oldValue = Lists.newArrayList(this.mavenArtifacts);
		mavenArtifacts.remove(mavenArtifact);
		firePropertyChangeAndMarkDirty(MAVEN_ARTEFACTS, oldValue, mavenArtifacts);
	}

	@Override
	public void addMavenArtifacts(MavenArtifact mavenArtifact) {
		final Object oldValue = Lists.newArrayList(this.mavenArtifacts);
		mavenArtifacts.add(mavenArtifact);
		firePropertyChangeAndMarkDirty(MAVEN_ARTEFACTS, oldValue, mavenArtifacts);
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
	public String getBaseVersionNumber() {
		return baseVersionNumber;
	}

	@Override
	public void setBaseVersionNumber(String baseVersionNumber) {
		this.baseVersionNumber = baseVersionNumber;
	}

	@Override
	public String getRevisionMnemoPart() {
		return revisionMnemoPart;
	}

	@Override
	public void setRevisionMnemoPart(String revisionMnemoPart) {
		this.revisionMnemoPart = revisionMnemoPart;
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
		return !getMavenArtifacts().isEmpty() || installOnEmptyModules;
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
	public String toString() {
		return "PatchBean [patchNummer=" + patchNummer + ", serviceName=" + serviceName + ", microServiceBranch="
				+ microServiceBranch + ", dbPatchBranch=" + dbPatchBranch + ", prodBranch=" + prodBranch + ", patchTag="
				+ patchTag + ", tagNr=" + tagNr + ", installationTarget=" + installationTarget + ", baseVersionNumber="
				+ baseVersionNumber + ", revisionMnemoPart=" + revisionMnemoPart + ", revision=" + revision
				+ ", lastRevision=" + lastRevision + ", runningNr=" + runningNr + ", dbObjects=" + dbObjects
				+ ", mavenArtifacts=" + mavenArtifacts + ", installOnEmptyModules=" + installOnEmptyModules + "]";
	}


}
