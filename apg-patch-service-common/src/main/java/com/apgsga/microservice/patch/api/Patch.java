package com.apgsga.microservice.patch.api;

import com.affichage.persistence.common.client.AbstractTransientEntity;
import com.affichage.persistence.common.client.EntityRootInterface;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.stream.Collectors;

@EntityRootInterface
public class Patch extends AbstractTransientEntity {

    private static final String PROD_BRANCH_DEFAULT = "prod";
    private static final long serialVersionUID = 1L;
    public static final String PATCH_NUMMER = "patchNummer";

    public static final String SERVICE_NAME = "serviceName";

    public static final String MICROSERVICE_BRANCH = "microServiceBranch";

    public static final String DB_PATCH_BRANCH = "dbPatchBranch";

    public static final String PROD_BRANCH = "prodBranch";

    public static final String DB_OBJECTS = "dbObjects";

    public static final String MAVEN_ARTEFACTS = "mavenArtifacts";

    public static final String DOCKER_SERVICES = "dockerServices";

    public static final String TAG_NR = "tagNr";

    public static final String PATCH_TAG = "patchTag";

    public static final String DEVELOPER_BRANCH = "developerBranch";

    public static final String INSTALLS_TARGET = "installationTarget";

    public static final String CURRENT_TARGET = "currentTarget";

    public static final String INSTALL_ON_EMPTY_MODULES = "installOnEmptyModules";

    public static final String INSTALL_JADAS_AND_GUI = "installJadasAndGui";

    public static final String INSTALL_DOCKER_SERVICES = "installDockerServices";

    public static final String PIPELINE_TASK = "pipeLineTask";

    public static final String LOG_TEXT = "logText";

    public static final String CURRENT_PIPELINE_TASK = "currentPipelineTask";


    private String patchNummer;
    private String serviceName = "It21Ui";
    private String microServiceBranch;
    private String dbPatchBranch;
    private String prodBranch = PROD_BRANCH_DEFAULT;
    private String patchTag = "";
    private String developerBranch = "";
    private Integer tagNr = 0;
    private String installationTarget;
    private String targetToState;
    private String baseVersionNumber;
    private String revisionMnemoPart;
    private String revision;
    private String lastRevision;
    private String runningNr;
    private List<DbObject> dbObjects = Lists.newArrayList();
    private List<MavenArtifact> mavenArtifacts = Lists.newArrayList();
    private List<String> dockerServices = Lists.newArrayList();
    private boolean installDockerServices = false;
    private boolean installOnEmptyModules = false;
    private String lastPipelineTask = "";
    private String currentTarget;
    private String currentPipelineTask;
    private String logText;

    public Patch() {
        super();
    }


    public String getPatchNummer() {
        return patchNummer;
    }


    public void setPatchNummer(String patchNummer) {
        final Object oldValue = this.patchNummer;
        this.patchNummer = patchNummer;
        firePropertyChangeEvent(PATCH_NUMMER, oldValue, patchNummer);
    }


    public String getServiceName() {
        return serviceName;
    }


    public void setServiceVersion(ServiceMetaData serviceVersion) {
        setServiceName(serviceVersion.getServiceName());
        setBaseVersionNumber(serviceVersion.getBaseVersionNumber());
        setRevisionMnemoPart(serviceVersion.getRevisionMnemoPart());
        setMicroServiceBranch(serviceVersion.getMicroServiceBranch());

    }


    public void setServiceName(String serviceName) {
        final Object oldValue = this.serviceName;
        this.serviceName = serviceName;
        firePropertyChangeAndMarkDirty(SERVICE_NAME, oldValue, serviceName);
    }


    public String getMicroServiceBranch() {
        return microServiceBranch;
    }


    public void setMicroServiceBranch(String microServiceBranch) {
        final Object oldValue = this.microServiceBranch;
        this.microServiceBranch = microServiceBranch;
        firePropertyChangeAndMarkDirty(MICROSERVICE_BRANCH, oldValue, microServiceBranch);
    }


    public String getDbPatchBranch() {
        return dbPatchBranch;
    }


    public void setDbPatchBranch(String dbPatchBranch) {
        final Object oldValue = this.dbPatchBranch;
        this.dbPatchBranch = dbPatchBranch;
        firePropertyChangeAndMarkDirty(DB_PATCH_BRANCH, oldValue, dbPatchBranch);
    }


    public String getProdBranch() {
        return prodBranch;
    }

    public void setProdBranch(String prodBranch) {
        final Object oldValue = this.prodBranch;
        this.prodBranch = prodBranch;
        firePropertyChangeAndMarkDirty(PROD_BRANCH, oldValue, prodBranch);
    }


    public String getDeveloperBranch() {
        return developerBranch;
    }


    public void setDeveloperBranch(String developerBranch) {
        final Object oldValue = this.developerBranch;
        this.developerBranch = developerBranch;
        firePropertyChangeAndMarkDirty(DEVELOPER_BRANCH, oldValue, developerBranch);
    }


    public List<DbObject> getDbObjects() {
        return dbObjects;
    }


    public List<String> getDbObjectsAsVcsPath() {
        return dbObjects.stream().map(DbObject::asFullPath).collect(Collectors.toList());
    }


    public void setDbObjects(List<DbObject> dbObjects) {
        final Object oldValue = Lists.newArrayList(this.dbObjects);
        this.dbObjects = dbObjects;
        firePropertyChangeEvent(DB_OBJECTS, oldValue, dbObjects);
    }


    public void removeDbObjects(DbObject dbObject) {
        final Object oldValue = Lists.newArrayList(this.dbObjects);
        dbObjects.remove(dbObject);
        firePropertyChangeAndMarkDirty(DB_OBJECTS, oldValue, dbObjects);
    }


    public void addDbObjects(DbObject dbObject) {
        final Object oldValue = Lists.newArrayList(this.dbObjects);
        dbObjects.add(dbObject);
        firePropertyChangeAndMarkDirty(DB_OBJECTS, oldValue, dbObjects);
    }


    public List<MavenArtifact> getMavenArtifacts() {
        return mavenArtifacts;
    }


    public List<String> getDockerServices() {
        return dockerServices;
    }


    public List<String> getMavenArtifactsAsVcsPath() {
        return getMavenArtifactsToBuild().stream().map(MavenArtifact::getName).collect(Collectors.toList());
    }


    public List<MavenArtifact> getMavenArtifactsToBuild() {
        return mavenArtifacts.stream().filter(m -> m.getVersion().endsWith("SNAPSHOT")).collect(Collectors.toList());
    }


    public void setMavenArtifacts(List<MavenArtifact> mavenArtifacts) {
        final Object oldValue = Lists.newArrayList(this.dbObjects);
        this.mavenArtifacts = mavenArtifacts;
        firePropertyChangeEvent(MAVEN_ARTEFACTS, oldValue, mavenArtifacts);

    }

    public void setDockerServices(List<String> dockerServices) {
        final Object oldValue = Lists.newArrayList(this.dockerServices);
        this.dockerServices = dockerServices;
        firePropertyChangeEvent(DOCKER_SERVICES, oldValue, dockerServices);
    }

    public void removeMavenArtifacts(MavenArtifact mavenArtifact) {
        final Object oldValue = Lists.newArrayList(this.mavenArtifacts);
        mavenArtifacts.remove(mavenArtifact);
        firePropertyChangeAndMarkDirty(MAVEN_ARTEFACTS, oldValue, mavenArtifacts);
    }

    public void addMavenArtifacts(MavenArtifact mavenArtifact) {
        final Object oldValue = Lists.newArrayList(this.mavenArtifacts);
        mavenArtifacts.add(mavenArtifact);
        firePropertyChangeAndMarkDirty(MAVEN_ARTEFACTS, oldValue, mavenArtifacts);
    }

    public void removeDockerService(String serviceName) {
        final Object oldValue = Lists.newArrayList(this.dockerServices);
        dockerServices.remove(serviceName);
        firePropertyChangeAndMarkDirty(DOCKER_SERVICES, oldValue, dockerServices);
    }

    public void addDockerService(String serviceName) {
        final Object oldValue = Lists.newArrayList(this.dockerServices);
        dockerServices.add(serviceName);
        firePropertyChangeAndMarkDirty(DOCKER_SERVICES, oldValue, dockerServices);
    }

    public void incrementTagNr() {
        Integer tagNr = getTagNr();
        setTagNr(++tagNr);
    }

    public Integer getTagNr() {
        return tagNr;
    }

    public void setTagNr(Integer tagNr) {
        final Object oldValue = this.tagNr;
        this.tagNr = tagNr;
        firePropertyChangeEvent(TAG_NR, oldValue, tagNr);
    }

    public String getPatchTag() {
        return patchTag;
    }

    public void setPatchTag(String patchTag) {
        final Object oldValue = this.patchTag;
        this.patchTag = patchTag;
        firePropertyChangeEvent(PATCH_TAG, oldValue, patchTag);
    }

    public String getInstallationTarget() {
        return installationTarget;
    }

    public void setInstallationTarget(String installationTarget) {
        final Object oldValue = this.installationTarget;
        this.installationTarget = installationTarget;
        firePropertyChangeEvent(INSTALLS_TARGET, oldValue, installationTarget);
    }

    public String getCurrentTarget() {
        return currentTarget;
    }

    public void setCurrentTarget(String currentTarget) {
        final Object oldValue = this.currentTarget;
        this.currentTarget = currentTarget;
        firePropertyChangeEvent(CURRENT_TARGET, oldValue, currentTarget);
    }

    public String getLogText() {
        return logText;
    }

    public void setLogText(String logText) {
        final Object oldValue = this.logText;
        this.logText = logText;
        firePropertyChange(LOG_TEXT, oldValue, logText);
    }

    public String getCurrentPipelineTask() {
        return currentPipelineTask;
    }

    public void setCurrentPipelineTask(String currentPipelineTask) {
        final Object oldValue = this.currentPipelineTask;
        this.currentPipelineTask = currentPipelineTask;
        firePropertyChange(CURRENT_PIPELINE_TASK, oldValue, currentPipelineTask);
    }

    public String getBaseVersionNumber() {
        return baseVersionNumber;
    }

    public void setBaseVersionNumber(String baseVersionNumber) {
        this.baseVersionNumber = baseVersionNumber;
    }

    public String getRevisionMnemoPart() {
        return revisionMnemoPart;
    }

    public void setRevisionMnemoPart(String revisionMnemoPart) {
        this.revisionMnemoPart = revisionMnemoPart;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revisionNumber) {
        this.revision = revisionNumber;
    }

    public String getLastRevision() {
        return lastRevision;
    }

    public void setLastRevision(String lastRevisionNumber) {
        this.lastRevision = lastRevisionNumber;
    }

    public boolean getInstallOnEmptyModules() {
        return installOnEmptyModules;
    }

    public void setInstallOnEmptyModules(boolean installOnEmptymodules) {
        final Object oldValue = this.installOnEmptyModules;
        this.installOnEmptyModules = installOnEmptymodules;
        firePropertyChangeEvent(INSTALL_ON_EMPTY_MODULES, oldValue, installOnEmptyModules);
    }

    public boolean getInstallJadasAndGui() {
        return !getMavenArtifacts().isEmpty() || installOnEmptyModules;
    }

    public void setInstallJadasAndGui() {
        // Intentionally empty
    }

    public boolean getInstallDockerServices() {
        return !dockerServices.isEmpty();
    }

    public void setInstallDockerServices() {
        // Intentionally empty
    }


    public String getRunningNr() {
        return this.runningNr;
    }

    public void setRunningNr(String runningNr) {
        this.runningNr = runningNr;
    }


    public String getTargetToState() {
        return targetToState;
    }

    public void setTargetToState(String targetToState) {
        this.targetToState = targetToState;
    }


    public String getLastPipelineTask() {
        return lastPipelineTask;
    }

    public void setLastPipelineTask(String pipelineTask) {
        this.lastPipelineTask = pipelineTask;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((baseVersionNumber == null) ? 0 : baseVersionNumber.hashCode());
        result = prime * result + ((dbObjects == null) ? 0 : dbObjects.hashCode());
        result = prime * result + ((dbPatchBranch == null) ? 0 : dbPatchBranch.hashCode());
        result = prime * result + ((developerBranch == null) ? 0 : developerBranch.hashCode());
        result = prime * result + (installOnEmptyModules ? 1231 : 1237);
        result = prime * result + ((installationTarget == null) ? 0 : installationTarget.hashCode());
        result = prime * result + ((lastRevision == null) ? 0 : lastRevision.hashCode());
        result = prime * result + ((mavenArtifacts == null) ? 0 : mavenArtifacts.hashCode());
        result = prime * result + ((microServiceBranch == null) ? 0 : microServiceBranch.hashCode());
        result = prime * result + ((patchNummer == null) ? 0 : patchNummer.hashCode());
        result = prime * result + ((patchTag == null) ? 0 : patchTag.hashCode());
        result = prime * result + ((lastPipelineTask == null) ? 0 : lastPipelineTask.hashCode());
        result = prime * result + ((prodBranch == null) ? 0 : prodBranch.hashCode());
        result = prime * result + ((revision == null) ? 0 : revision.hashCode());
        result = prime * result + ((revisionMnemoPart == null) ? 0 : revisionMnemoPart.hashCode());
        result = prime * result + ((runningNr == null) ? 0 : runningNr.hashCode());
        result = prime * result + ((serviceName == null) ? 0 : serviceName.hashCode());
        result = prime * result + ((tagNr == null) ? 0 : tagNr.hashCode());
        result = prime * result + ((targetToState == null) ? 0 : targetToState.hashCode());
        result = prime * result + ((currentTarget == null) ? 0 : currentTarget.hashCode());
        result = prime * result + ((currentPipelineTask == null) ? 0 : currentPipelineTask.hashCode());
        result = prime * result + ((logText == null) ? 0 : logText.hashCode());
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
        Patch other = (Patch) obj;
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
        if (developerBranch == null) {
            if (other.developerBranch != null)
                return false;
        } else if (!developerBranch.equals(other.developerBranch))
            return false;
        if (installOnEmptyModules != other.installOnEmptyModules)
            return false;
        if (installationTarget == null) {
            if (other.installationTarget != null)
                return false;
        } else if (!installationTarget.equals(other.installationTarget))
            return false;
        if (lastRevision == null) {
            if (other.lastRevision != null)
                return false;
        } else if (!lastRevision.equals(other.lastRevision))
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
        if (lastPipelineTask == null) {
            if (other.lastPipelineTask != null)
                return false;
        } else if (!lastPipelineTask.equals(other.lastPipelineTask))
            return false;
        if (prodBranch == null) {
            if (other.prodBranch != null)
                return false;
        } else if (!prodBranch.equals(other.prodBranch))
            return false;
        if (revision == null) {
            if (other.revision != null)
                return false;
        } else if (!revision.equals(other.revision))
            return false;
        if (revisionMnemoPart == null) {
            if (other.revisionMnemoPart != null)
                return false;
        } else if (!revisionMnemoPart.equals(other.revisionMnemoPart))
            return false;
        if (runningNr == null) {
            if (other.runningNr != null)
                return false;
        } else if (!runningNr.equals(other.runningNr))
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
        if (targetToState == null) {
            if (other.targetToState != null)
                return false;
        } else if (!targetToState.equals(other.targetToState))
            return false;
        if (currentTarget == null) {
            if (other.currentTarget != null)
                return false;
        } else if (!currentTarget.equals(other.currentTarget))
            return false;
        if (currentPipelineTask == null) {
            if (other.getCurrentPipelineTask() != null)
                return false;
        } else if (!currentPipelineTask.equals(other.getCurrentPipelineTask()))
            return false;
        if (logText == null) {
            return other.getLogText() == null;
        } else return logText.equals(other.getLogText());
    }

    @Override
    public String toString() {
        return "PatchBean [patchNummer=" + patchNummer + ", serviceName=" + serviceName + ", microServiceBranch="
                + microServiceBranch + ", dbPatchBranch=" + dbPatchBranch + ", prodBranch=" + prodBranch + ", patchTag="
                + patchTag + ", developerBranch=" + developerBranch + ", tagNr=" + tagNr + ", installationTarget="
                + installationTarget + ", targetToState=" + targetToState + ", baseVersionNumber=" + baseVersionNumber
                + ", revisionMnemoPart=" + revisionMnemoPart + ", revision=" + revision + ", lastRevision="
                + lastRevision + ", runningNr=" + runningNr + ", dbObjects=" + dbObjects + ", mavenArtifacts="
                + mavenArtifacts + ", installOnEmptyModules=" + installOnEmptyModules + ", pipelineTask=" + lastPipelineTask
                + ", currentTarget=" + currentTarget + ", currentPipelineTask=" + currentPipelineTask + ", logText=" + logText
                + "]";
    }
}
