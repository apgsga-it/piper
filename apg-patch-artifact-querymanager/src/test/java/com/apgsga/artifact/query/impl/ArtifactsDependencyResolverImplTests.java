package com.apgsga.artifact.query.impl;

import static org.junit.Assert.*;

import org.assertj.core.util.Lists;
import org.junit.Test;

import com.apgsga.microservice.patch.api.impl.MavenArtifactBean;

public class ArtifactsDependencyResolverImplTests {

	@Test
	public void testSingleArtifact() {
		ArtifactsDependencyResolverImpl depResolver = new ArtifactsDependencyResolverImpl("target/maverepo");
		depResolver.resolveDependencies(Lists.newArrayList(new MavenArtifactBean("gp-ui","com.affichage.it21.gp","9.0.6.ADMIN-UIMIG-SNAPSHOT")));
	}
	
	@Test
	public void testTwoArtifacts() {
		ArtifactsDependencyResolverImpl depResolver = new ArtifactsDependencyResolverImpl("target/maverepo");
		depResolver.resolveDependencies(Lists.newArrayList(new MavenArtifactBean("gp-ui","com.affichage.it21.gp","9.0.6.ADMIN-UIMIG-SNAPSHOT"),new MavenArtifactBean("gp-dao","com.affichage.it21.gp","9.0.6.ADMIN-UIMIG-SNAPSHOT")));
	}

}
