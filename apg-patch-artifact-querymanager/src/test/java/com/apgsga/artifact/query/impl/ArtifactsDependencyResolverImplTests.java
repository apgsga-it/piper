package com.apgsga.artifact.query.impl;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.impl.MavenArtifactBean;
import com.google.common.collect.Lists;

public class ArtifactsDependencyResolverImplTests {

	@Test
	public void testSingleArtifact() {
		ArtifactsDependencyResolverImpl depResolver = new ArtifactsDependencyResolverImpl("target/maverepo");
		MavenArtifactBean mavenArtifactBean = new MavenArtifactBean("gp-ui","com.affichage.it21.gp","9.0.6.ADMIN-UIMIG-SNAPSHOT");
		List<MavenArtWithDependencies> result = depResolver.resolveDependencies(Lists.newArrayList(mavenArtifactBean));
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
		List<MavenArtWithDependencies> result = depResolver.resolveDependencies(Lists.newArrayList(mavenArtifactGpUi,mavenArtifactGpDao));
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
		List<MavenArtWithDependencies> result = depResolver.resolveDependencies(Lists.newArrayList(mavenArtifactGpUi,mavenArtifactGpDao,mavenArtifactDt,mavenArtifactFakturaDao));
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

}
