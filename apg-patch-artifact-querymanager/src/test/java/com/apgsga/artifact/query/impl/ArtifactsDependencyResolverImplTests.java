package com.apgsga.artifact.query.impl;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.impl.MavenArtifactBean;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public class ArtifactsDependencyResolverImplTests {

	@Test
	public void testSingleArtifact() {
		ArtifactsDependencyResolverImpl depResolver = new ArtifactsDependencyResolverImpl("target/maverepo");
		MavenArtifactBean mavenArtifactBean = new MavenArtifactBean("gp-ui","com.affichage.it21.gp","9.0.6.ADMIN-UIMIG-SNAPSHOT");
		List<MavenArtWithDependencies> result = depResolver.resolveDependenciesInternal(Lists.newArrayList(mavenArtifactBean));
		assert(result.size() == 1);
		MavenArtWithDependencies mavenArt = result.get(0);
		assertEquals(mavenArtifactBean, mavenArt.getArtifact());
		assert(mavenArt.getDependencies().size() == 0);
	}
	
	@Test
	public void testTwoArtifacts() {
		ArtifactsDependencyResolverImpl depResolver = new ArtifactsDependencyResolverImpl("target/maverepo");
		MavenArtifactBean mavenArtifactGpUi = new MavenArtifactBean("gp-ui","com.affichage.it21.gp","9.0.6.ADMIN-UIMIG-SNAPSHOT");
		MavenArtifactBean mavenArtifactGpDao = new MavenArtifactBean("gp-dao","com.affichage.it21.gp","9.0.6.ADMIN-UIMIG-SNAPSHOT");
		List<MavenArtWithDependencies> result = depResolver.resolveDependenciesInternal(Lists.newArrayList(mavenArtifactGpUi,mavenArtifactGpDao));
		assert(result.size() == 2);
		MavenArtWithDependencies mavenArtGpUi = result.get(0);
		assertEquals(mavenArtifactGpUi, mavenArtGpUi.getArtifact());
		List<MavenArtifact> gpUiDependencies = mavenArtGpUi.getDependencies();
		assert(gpUiDependencies.size() == 1);
		MavenArtifact gpUiDependency = gpUiDependencies.get(0);
		assertEquals(mavenArtifactGpDao, gpUiDependency);
		MavenArtWithDependencies mavenArtGpDao = result.get(1);
		assertEquals(mavenArtifactGpDao, mavenArtGpDao.getArtifact());
		assert(mavenArtGpDao.getDependencies().size() == 0);
	}
	
	@Test
	public void testMoreArtifactsNestedDependencies() {
		ArtifactsDependencyResolverImpl depResolver = new ArtifactsDependencyResolverImpl("target/maverepo");
		MavenArtifactBean mavenArtifactDt = new MavenArtifactBean("datetime-utils","com.affichage.datetime","9.0.6.ADMIN-UIMIG-SNAPSHOT");
		MavenArtifactBean mavenArtifactGpUi = new MavenArtifactBean("gp-ui","com.affichage.it21.gp","9.0.6.ADMIN-UIMIG-SNAPSHOT");
		MavenArtifactBean mavenArtifactGpDao = new MavenArtifactBean("gp-dao","com.affichage.it21.gp","9.0.6.ADMIN-UIMIG-SNAPSHOT");
		MavenArtifactBean mavenArtifactFakturaDao = new MavenArtifactBean("faktura-dao","com.affichage.it21.vk","9.0.6.ADMIN-UIMIG-SNAPSHOT");
		ArrayList<MavenArtifact> artefacts = Lists.newArrayList(mavenArtifactGpUi,mavenArtifactGpDao,mavenArtifactDt,mavenArtifactFakturaDao);
		List<MavenArtWithDependencies> result = depResolver.resolveDependenciesInternal(artefacts);
		assert(result.size() == 4);
		MavenArtWithDependencies mavenArtGpUi = result.get(0);
		assertEquals(mavenArtifactGpUi, mavenArtGpUi.getArtifact());
		List<MavenArtifact> gpUiDependencies = mavenArtGpUi.getDependencies();
		assert(gpUiDependencies.size() == 2);
		assert(gpUiDependencies.contains(mavenArtifactDt)); 
		assert(gpUiDependencies.contains(mavenArtifactGpDao)); 
		MavenArtWithDependencies mavenArtGpDao = result.get(1);
		List<MavenArtifact> gpDaoDependencies = mavenArtGpDao.getDependencies();
		assert(gpDaoDependencies.size() == 1);
		assert(gpDaoDependencies.contains(mavenArtifactDt)); 
		MavenArtWithDependencies mavenArtDt = result.get(2);
		List<MavenArtifact> dtDependencies = mavenArtDt.getDependencies();
		assert(dtDependencies.size() == 0);
		MavenArtWithDependencies mavenArtFakturaDao = result.get(3);
		assertEquals(mavenArtifactFakturaDao, mavenArtFakturaDao.getArtifact());
		List<MavenArtifact> fakturaDaoDependencies = mavenArtFakturaDao.getDependencies();
		assert(fakturaDaoDependencies.size() == 2);
		assert(gpUiDependencies.contains(mavenArtifactDt)); 
		assert(gpUiDependencies.contains(mavenArtifactGpDao)); 
	}
	
	@Test
	public void testTwoArtifactsWithDependencyLevel() {
		ArtifactsDependencyResolverImpl depResolver = new ArtifactsDependencyResolverImpl("target/maverepo");
		MavenArtifactBean mavenArtifactGpUi = new MavenArtifactBean("gp-ui","com.affichage.it21.gp","9.0.6.ADMIN-UIMIG-SNAPSHOT");
		MavenArtifactBean mavenArtifactGpDao = new MavenArtifactBean("gp-dao","com.affichage.it21.gp","9.0.6.ADMIN-UIMIG-SNAPSHOT");
		ArrayList<MavenArtifact> artefacts = Lists.newArrayList(mavenArtifactGpUi,mavenArtifactGpDao);
		depResolver.resolveDependencies(artefacts);
		artefacts.sort(Comparator.comparing(MavenArtifact::getDependencyLevel).reversed());
		artefacts.forEach(a -> System.out.println("Dependency Level: " + a.getDependencyLevel() + " for Artefact : " + a.toString()));
		assertEquals(Integer.valueOf(0),mavenArtifactGpUi.getDependencyLevel());
		assertEquals(Integer.valueOf(1),mavenArtifactGpDao.getDependencyLevel());
	}
	
	@Test
	public void testMoreArtifactsNestedDependenciesWithDependencyLevel() {
		ArtifactsDependencyResolverImpl depResolver = new ArtifactsDependencyResolverImpl("target/maverepo");
		MavenArtifactBean mavenArtifactDt = new MavenArtifactBean("datetime-utils","com.affichage.datetime","9.0.6.ADMIN-UIMIG-SNAPSHOT");
		MavenArtifactBean mavenArtifactGpUi = new MavenArtifactBean("gp-ui","com.affichage.it21.gp","9.0.6.ADMIN-UIMIG-SNAPSHOT");
		MavenArtifactBean mavenArtifactGpDao = new MavenArtifactBean("gp-dao","com.affichage.it21.gp","9.0.6.ADMIN-UIMIG-SNAPSHOT");
		MavenArtifactBean mavenArtifactFakturaDao = new MavenArtifactBean("faktura-dao","com.affichage.it21.vk","9.0.6.ADMIN-UIMIG-SNAPSHOT");
		ArrayList<MavenArtifact> artefacts = Lists.newArrayList(mavenArtifactGpUi,mavenArtifactGpDao,mavenArtifactDt,mavenArtifactFakturaDao);
		depResolver.resolveDependencies(artefacts);
		artefacts.sort(Comparator.comparing(MavenArtifact::getDependencyLevel).reversed());
		artefacts.forEach(a -> System.out.println("Dependency Level: " + a.getDependencyLevel() + " for Artefact : " + a.toString()));
		assertEquals(Integer.valueOf(3),mavenArtifactDt.getDependencyLevel());
		assertEquals(Integer.valueOf(2),mavenArtifactGpDao.getDependencyLevel());
		assertEquals(Integer.valueOf(0),mavenArtifactGpUi.getDependencyLevel());
		assertEquals(Integer.valueOf(0),mavenArtifactFakturaDao.getDependencyLevel());


	}
	
	
	@Test
	public void testMegaPatchWithDependencyLevels() throws JsonParseException, JsonMappingException, IOException {
		ArtifactsDependencyResolverImpl depResolver = new ArtifactsDependencyResolverImpl("target/maverepo");
		File patchFile = new File("src/test/resources/Patch5708.json");
		ObjectMapper mapper = new ObjectMapper();
		Patch patchData = mapper.readValue(patchFile, Patch.class);
		List<MavenArtifact> artefacts = patchData.getMavenArtifacts();
		depResolver.resolveDependencies(artefacts);
		artefacts.sort(Comparator.comparing(MavenArtifact::getDependencyLevel).reversed());
		artefacts.forEach(a -> System.out.println("Dependency Level: " + a.getDependencyLevel() + " for Artefact : " + a.toString()));
		File newPatchFile = new File("src/test/resources/Patch5708new.json");
		mapper.writeValue(new FileWriter(newPatchFile),patchData);
	}


}
