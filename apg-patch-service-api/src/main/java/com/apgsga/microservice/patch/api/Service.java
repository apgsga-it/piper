package com.apgsga.microservice.patch.api;

import com.affichage.persistence.common.client.AbstractTransientEntity;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Service  extends AbstractTransientEntity {

    public static Service create(ServiceMetaData metaData) {
        return new Service(metaData);
    }

    public static Service create() {
        return new Service();
    }
    private static final long serialVersionUID = 1L;
    public static final String SERVICE_NAME = "serviceName";
    public static final String MICRO_SERVICE_BRANCH = "microServiceBranch";
    public static final String BASE_VERSION_NUMBER = "baseVersionNumber";
    public static final String REVISION_MNEMO_PART = "revisionMnemoPart";
    public static final String MAVEN_ARTIFACTS = "mavenArtifacts";
    public static final String INSTALL_ON_EMPTY_MODULES = "installOnEmptyModules";
    public static final String REVISION = "revision";
    public static final String LAST_REVISION = "lastRevision";
    private static final String PACKAGER_NAME = "packagerName";

    private String serviceName;
    private String microServiceBranch;
    private String baseVersionNumber;
    private String revisionMnemoPart;
    private boolean installOnEmptyModules = false;
    private String revision;
    private String lastRevision;
    private List<MavenArtifact> mavenArtifacts = Lists.newArrayList();
    private String packagerName;

    public Service(String serviceName, String microServiceBranch, String baseVersionNumber, String revisionMnemoPart) {
        this.serviceName = serviceName;
        this.microServiceBranch = microServiceBranch;
        this.baseVersionNumber = baseVersionNumber;
        this.revisionMnemoPart = revisionMnemoPart;
    }

    public Service(ServiceMetaData metaData) {
        this.serviceName = metaData.getServiceName();
        this.microServiceBranch = metaData.getMicroServiceBranch();
        this.baseVersionNumber = metaData.getBaseVersionNumber();
        this.revisionMnemoPart = metaData.getRevisionMnemoPart();
    }


    public Service() {
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        final Object oldValue = this.serviceName;
        this.serviceName = serviceName;
        firePropertyChangeAndMarkDirty(SERVICE_NAME, oldValue, serviceName);
    }

    public Service serviceName(String serviceName) {
        setServiceName(serviceName);
        return this;
    }

    public String getMicroServiceBranch() {
        return microServiceBranch;
    }

    public void setMicroServiceBranch(String microServiceBranch) {
        final Object oldValue = this.microServiceBranch;
        this.microServiceBranch = microServiceBranch;
        firePropertyChangeAndMarkDirty(MICRO_SERVICE_BRANCH, oldValue, microServiceBranch);
    }

    public Service microServiceBranch(String microServiceBranch) {
        setMicroServiceBranch(microServiceBranch);
        return this;
    }

    public String getBaseVersionNumber() {
        return baseVersionNumber;
    }

    public void setBaseVersionNumber(String baseVersionNumber) {
        final Object oldValue = this.baseVersionNumber;
        this.baseVersionNumber = baseVersionNumber;
        firePropertyChangeAndMarkDirty(BASE_VERSION_NUMBER, oldValue, baseVersionNumber);
    }

    public Service baseVersionNumber(String baseVersionNumber) {
        setBaseVersionNumber(baseVersionNumber);
        return this;
    }

    public String getRevisionMnemoPart() {
        return revisionMnemoPart;
    }

    public void setRevisionMnemoPart(String revisionMnemoPart) {
        final Object oldValue = this.revisionMnemoPart;
        this.revisionMnemoPart = revisionMnemoPart;
        firePropertyChangeAndMarkDirty(REVISION_MNEMO_PART, oldValue, revisionMnemoPart);
    }
    public Service revisionMnemoPart(String revisionMnemoPart) {
        setRevisionMnemoPart(revisionMnemoPart);
        return this;
    }

    public List<MavenArtifact> getMavenArtifacts() {
        return mavenArtifacts;
    }

    public void setMavenArtifacts(List<MavenArtifact> mavenArtifacts) {
        final Object oldValue = this.mavenArtifacts;
        this.mavenArtifacts = mavenArtifacts;
        firePropertyChangeAndMarkDirty(MAVEN_ARTIFACTS, oldValue, mavenArtifacts);
    }

    public void removeMavenArtifacts(MavenArtifact mavenArtifact) {
        final Object oldValue = Lists.newArrayList(this.mavenArtifacts);
        mavenArtifacts.remove(mavenArtifact);
        firePropertyChangeAndMarkDirty(MAVEN_ARTIFACTS, oldValue, mavenArtifacts);
    }

    public void addMavenArtifacts(MavenArtifact mavenArtifact) {
        final Object oldValue = Lists.newArrayList(this.mavenArtifacts);
        mavenArtifacts.add(mavenArtifact);
        firePropertyChangeAndMarkDirty(MAVEN_ARTIFACTS, oldValue, mavenArtifacts);
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revisionNumber) {
        final Object oldValue = this.revision;
        this.revision = revisionNumber;
        firePropertyChangeAndMarkDirty(REVISION, oldValue, revision);
    }

    public String getLastRevision() {
        return lastRevision;
    }

    public void setLastRevision(String lastRevisionNumber) {
        final Object oldValue = this.lastRevision;
        this.lastRevision = lastRevisionNumber;
        firePropertyChangeAndMarkDirty(LAST_REVISION, oldValue, lastRevision);
    }

    public String getPackagerName() {
        return packagerName;
    }

    public void setPackagerName(String packagerName) {
        final Object oldValue = this.packagerName;
        this.packagerName = packagerName;
        firePropertyChangeAndMarkDirty(PACKAGER_NAME, oldValue, packagerName);
    }

    public List<String> getMavenArtifactsAsVcsPath() {
        return getMavenArtifactsToBuild().stream().map(MavenArtifact::getName).collect(Collectors.toList());
    }


    public List<MavenArtifact> getMavenArtifactsToBuild() {
        return mavenArtifacts.stream().filter(m -> m.getVersion().endsWith("SNAPSHOT")).collect(Collectors.toList());
    }

    public void setInstallJadasAndGui() {
        // Intentionally empty
    }

    public boolean getInstallJadasAndGui() {
        return !getMavenArtifacts().isEmpty() || installOnEmptyModules;
    }

    public boolean getInstallOnEmptyModules() {
        return installOnEmptyModules;
    }

    public void setInstallOnEmptyModules(boolean installOnEmptymodules) {
        final Object oldValue = this.installOnEmptyModules;
        this.installOnEmptyModules = installOnEmptymodules;
        firePropertyChangeEvent(INSTALL_ON_EMPTY_MODULES, oldValue, installOnEmptyModules);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Service)) return false;
        Service service = (Service) o;
        return serviceName.equals(service.serviceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName);
    }

    @Override
    public String toString() {
        return "Service{" +
                "serviceName='" + serviceName + '\'' +
                ", microServiceBranch='" + microServiceBranch + '\'' +
                ", baseVersionNumber='" + baseVersionNumber + '\'' +
                ", revisionMnemoPart='" + revisionMnemoPart + '\'' +
                ", mavenArtifacts=" + mavenArtifacts +
                '}';
    }
}
