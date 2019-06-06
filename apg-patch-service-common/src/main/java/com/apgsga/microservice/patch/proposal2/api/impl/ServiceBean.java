package com.apgsga.microservice.patch.proposal2.api.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.affichage.persistence.common.client.AbstractTransientEntity;
import com.apgsga.microservice.patch.proposal2.api.MavenArtifact;
import com.apgsga.microservice.patch.proposal2.api.Service;
import com.apgsga.microservice.patch.proposal2.api.ServiceMetaData;
import com.google.common.collect.Lists;

public class ServiceBean extends AbstractTransientEntity implements Service {


	private static final long serialVersionUID = 1L;
	private ServiceMetaData service;
	private List<MavenArtifact> mavenArtifacts = Lists.newArrayList();

	@Override
	public void setServiceMetaData(ServiceMetaData serviceData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ServiceMetaData getServiceMetaData() {
		// TODO Auto-generated method stub
		return null;
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
		final Object oldValue = Lists.newArrayList(this.mavenArtifacts);
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

}
