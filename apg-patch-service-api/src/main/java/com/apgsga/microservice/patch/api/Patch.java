package com.apgsga.microservice.patch.api;

import com.affichage.persistence.common.client.AbstractTransientEntity;
import com.affichage.persistence.common.client.EntityRootInterface;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@EntityRootInterface
public class Patch extends AbstractTransientEntity {

    private static final String PROD_BRANCH_DEFAULT = "prod";
    private static final long serialVersionUID = 1L;

    public static final String PATCH_NUMMER = "patchNummer";

    public static final String DB_PATCH_BRANCH = "dbPatchBranch";

    public static final String PROD_BRANCH = "prodBranch";

    public static final String DB_OBJECTS = "dbObjects";

    public static final String DOCKER_SERVICES = "dockerServices";

    public static final String SERVICES = "services";

    public static final String TAG_NR = "tagNr";

    public static final String PATCH_TAG = "patchTag";

    public static final String DEVELOPER_BRANCH = "developerBranch";

    public static final String LOG_TEXT = "logText";

    public static final String STAGES_MAPPING = "stagesMapping";


    private String patchNummer;
    private String dbPatchBranch;
    private String prodBranch = PROD_BRANCH_DEFAULT;
    private String patchTag = "";
    private String developerBranch = "";
    private Integer tagNr = 0;
    private List<DbObject> dbObjects = Lists.newArrayList();
    private List<String> dockerServices = Lists.newArrayList();
    private List<Service> services = Lists.newArrayList();
    private boolean installDockerServices = false;
    private String logText;
    private List<StageMapping> stagesMapping;

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

    public List<String> getDockerServices() {
        return dockerServices;
    }

    public void setDockerServices(List<String> dockerServices) {
        final Object oldValue = Lists.newArrayList(this.dockerServices);
        this.dockerServices = dockerServices;
        firePropertyChangeEvent(DOCKER_SERVICES, oldValue, dockerServices);
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

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        final Object oldValue = Lists.newArrayList(this.services);
        this.services = services;
        firePropertyChangeEvent(SERVICES, oldValue, services);
        this.services = services;
    }

    public void removeServices(Service service) {
        final Object oldValue = Lists.newArrayList(this.services);
        services.remove(service);
        firePropertyChangeAndMarkDirty(SERVICES, oldValue, services);
    }

    public void addServices(Service service) {
        final Object oldValue = Lists.newArrayList(this.services);
        services.add(service);
        firePropertyChangeAndMarkDirty(SERVICES, oldValue, services);
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

    public String getLogText() {
        return logText;
    }

    public void setLogText(String logText) {
        final Object oldValue = this.logText;
        this.logText = logText;
        firePropertyChange(LOG_TEXT, oldValue, logText);
    }

    public List<StageMapping> getStagesMapping() {
        return stagesMapping;
    }

    public void setStagesMapping(List<StageMapping> stagesMapping) {
        final Object oldValue = this.stagesMapping;
        this.stagesMapping = stagesMapping;
        firePropertyChange(STAGES_MAPPING, oldValue, stagesMapping);
    }

    public Service getService(String serviceName) {
        for (Service service : services) {
            if (service.getServiceName().equals(serviceName)) {
                return service;
            }
        }
        return null;
    }

   // TODO (MULTISERVICE_CM , 9.4) : This is here for backward compatability and must go away
    public List<MavenArtifact> getMavenArtifacts() {
        List<MavenArtifact> all = Lists.newArrayList();
        for (Service service : services) all.addAll(service.getMavenArtifacts());
        return all;
    }

    public boolean getInstallDockerServices() {
        return !dockerServices.isEmpty();
    }

    public void setInstallDockerServices() {
        // Intentionally empty
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Patch)) return false;
        Patch patch = (Patch) o;
        return patchNummer.equals(patch.patchNummer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patchNummer);
    }

    @Override
    public String toString() {
        return "Patch{" +
                "patchNummer='" + patchNummer + '\'' +
                ", dbPatchBranch='" + dbPatchBranch + '\'' +
                ", prodBranch='" + prodBranch + '\'' +
                ", patchTag='" + patchTag + '\'' +
                ", developerBranch='" + developerBranch + '\'' +
                ", tagNr=" + tagNr +
                ", dbObjects=" + dbObjects +
                ", dockerServices=" + dockerServices +
                ", services=" + services +
                ", installDockerServices=" + installDockerServices +
                ", logText='" + logText + '\'' +
                '}';
    }
}
