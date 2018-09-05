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
	private String revisionNumber;
	private String lastRevisionNumber;
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
		return mavenArtifacts.stream().map(mavenArt -> mavenArt.getName()).collect(Collectors.toList());

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
	public String getRevisionNumber() {
		return revisionNumber;
	}

	@Override
	public void setRevisionNumber(String revisionNumber) {
		this.revisionNumber = revisionNumber;
	}

	@Override
	public String getLastRevisionNumber() {
		return lastRevisionNumber;
	}

	@Override
	public void setLastRevisionNumber(String lastRevisionNumber) {
		this.lastRevisionNumber = lastRevisionNumber;
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
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((baseVersionNumber == null) ? 0 : baseVersionNumber.hashCode());
		result = prime * result + ((dbObjects == null) ? 0 : dbObjects.hashCode());
		result = prime * result + ((dbPatchBranch == null) ? 0 : dbPatchBranch.hashCode());
		result = prime * result + (installOnEmptyModules ? 1231 : 1237);
		result = prime * result + ((installationTarget == null) ? 0 : installationTarget.hashCode());
		result = prime * result + ((lastRevisionNumber == null) ? 0 : lastRevisionNumber.hashCode());
		result = prime * result + ((mavenArtifacts == null) ? 0 : mavenArtifacts.hashCode());
		result = prime * result + ((microServiceBranch == null) ? 0 : microServiceBranch.hashCode());
		result = prime * result + ((patchNummer == null) ? 0 : patchNummer.hashCode());
		result = prime * result + ((patchTag == null) ? 0 : patchTag.hashCode());
		result = prime * result + ((prodBranch == null) ? 0 : prodBranch.hashCode());
		result = prime * result + ((revisionMnemoPart == null) ? 0 : revisionMnemoPart.hashCode());
		result = prime * result + ((revisionNumber == null) ? 0 : revisionNumber.hashCode());
		result = prime * result + ((serviceName == null) ? 0 : serviceName.hashCode());
		result = prime * result + ((tagNr == null) ? 0 : tagNr.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PatchBean other = (PatchBean) obj;
		if (baseVersionNumber == null) {
			if (other.baseVersionNumber != null)
				return false;
		} else if (!baseVersionNumber.equals(other.baseVersionNumber))
			return false;
		if (dbObjects == null) {
			if (other.dbObjects != null)
				return false;
		} else if (!dbObjects.equals(other.dbObjects))
			return false;
		if (dbPatchBranch == null) {
			if (other.dbPatchBranch != null)
				return false;
		} else if (!dbPatchBranch.equals(other.dbPatchBranch))
			return false;
		if (installOnEmptyModules != other.installOnEmptyModules)
			return false;
		if (installationTarget == null) {
			if (other.installationTarget != null)
				return false;
		} else if (!installationTarget.equals(other.installationTarget))
			return false;
		if (lastRevisionNumber == null) {
			if (other.lastRevisionNumber != null)
				return false;
		} else if (!lastRevisionNumber.equals(other.lastRevisionNumber))
			return false;
		if (mavenArtifacts == null) {
			if (other.mavenArtifacts != null)
				return false;
		} else if (!mavenArtifacts.equals(other.mavenArtifacts))
			return false;
		if (microServiceBranch == null) {
			if (other.microServiceBranch != null)
				return false;
		} else if (!microServiceBranch.equals(other.microServiceBranch))
			return false;
		if (patchNummer == null) {
			if (other.patchNummer != null)
				return false;
		} else if (!patchNummer.equals(other.patchNummer))
			return false;
		if (patchTag == null) {
			if (other.patchTag != null)
				return false;
		} else if (!patchTag.equals(other.patchTag))
			return false;
		if (prodBranch == null) {
			if (other.prodBranch != null)
				return false;
		} else if (!prodBranch.equals(other.prodBranch))
			return false;
		if (revisionMnemoPart == null) {
			if (other.revisionMnemoPart != null)
				return false;
		} else if (!revisionMnemoPart.equals(other.revisionMnemoPart))
			return false;
		if (revisionNumber == null) {
			if (other.revisionNumber != null)
				return false;
		} else if (!revisionNumber.equals(other.revisionNumber))
			return false;
		if (serviceName == null) {
			if (other.serviceName != null)
				return false;
		} else if (!serviceName.equals(other.serviceName))
			return false;
		if (tagNr == null) {
			if (other.tagNr != null)
				return false;
		} else if (!tagNr.equals(other.tagNr))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PatchBean [patchNummer=" + patchNummer + ", serviceName=" + serviceName + ", microServiceBranch="
				+ microServiceBranch + ", dbPatchBranch=" + dbPatchBranch + ", prodBranch=" + prodBranch + ", patchTag="
				+ patchTag + ", tagNr=" + tagNr + ", installationTarget=" + installationTarget + ", baseVersionNumber="
				+ baseVersionNumber + ", revisionMnemoPart=" + revisionMnemoPart + ", revisionNumber=" + revisionNumber
				+ ", lastRevisionNumber=" + lastRevisionNumber + ", dbObjects=" + dbObjects + ", mavenArtifacts="
				+ mavenArtifacts + ", installOnEmptyModules=" + installOnEmptyModules + "]";
	}

}
