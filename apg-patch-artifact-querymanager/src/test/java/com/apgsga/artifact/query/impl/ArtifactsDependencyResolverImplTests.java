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
		assert(mavenArtGpUi.getDependencies().size() == 1);
		MavenArtifact dependency = mavenArtGpUi.getDependencies().get(0);
		assertEquals(dependency,mavenArtifactGpDao);
		MavenArtWithDependencies mavenArtGpDao = result.get(1);
		assertEquals(mavenArtifactGpDao, mavenArtGpDao.getArtifact());
		assert(mavenArtGpDao.getDependencies().size() == 0);
	}

}
