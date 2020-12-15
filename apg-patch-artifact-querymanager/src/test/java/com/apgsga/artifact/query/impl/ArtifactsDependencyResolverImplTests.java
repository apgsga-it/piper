package com.apgsga.artifact.query.impl;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.apgsga.artifact.query.RepositorySystemFactory;
import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.test.config.TestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

@ContextConfiguration(classes = TestConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ArtifactsDependencyResolverImplTests {
	
	@Value("${mavenrepo.user.name}")
	String repoUser;
	
	@Value("${mavenrepo.baseurl}")
	String repoUrl;
	
	@Value("${mavenrepo.name}")
	String repoName;
	
	@Value("${mavenrepo.user.encryptedPwd}")
	String mavenRepoUserEncryptedPwd;

	@Value("${mavenrepo.user.decryptpwd.key:}")
	private String mavenRepoUserDecryptKey;
	
	RepositorySystemFactory systemFactory;
	
	@Before
	public void before() {
		systemFactory = RepositorySystemFactory.create(repoUrl, repoName, repoUser, mavenRepoUserEncryptedPwd, mavenRepoUserDecryptKey);
	}

	@Test
	public void testNoArtifact() {
		ArtifactsDependencyResolverImpl depResolver = new ArtifactsDependencyResolverImpl("target/maverepo", systemFactory);
		List<MavenArtWithDependencies> result = depResolver.resolveDependenciesInternal(Lists.newArrayList());
		assert(result.size() == 0);		
	}
	
	@Test
	public void testSingleArtifact() {
		ArtifactsDependencyResolverImpl depResolver = new ArtifactsDependencyResolverImpl("target/maverepo", systemFactory);
		MavenArtifact mavenArtifactBean = MavenArtifact.builder().artifactId("gp-ui").groupId("com.affichage.it21.gp").version("9.1.0.ADMIN-UIMIG-SNAPSHOT").build();
		List<MavenArtWithDependencies> result = depResolver.resolveDependenciesInternal(Lists.newArrayList(mavenArtifactBean));
		assert(result.size() == 1);
		MavenArtWithDependencies mavenArt = result.get(0);
		assertEquals(mavenArtifactBean, mavenArt.getArtifact());
		assert(mavenArt.getDependencies().size() == 0);
	}
	
	@Test
	public void testTwoArtifacts() {
		ArtifactsDependencyResolverImpl depResolver = new ArtifactsDependencyResolverImpl("target/maverepo", systemFactory);
		MavenArtifact mavenArtifactGpUi = MavenArtifact.builder().artifactId("gp-ui").groupId("com.affichage.it21.gp").version("9.1.0.ADMIN-UIMIG-SNAPSHOT").build();
		MavenArtifact mavenArtifactGpDao = MavenArtifact.builder().artifactId("gp-dao").groupId("com.affichage.it21.gp").version("9.1.0.ADMIN-UIMIG-SNAPSHOT").build();
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
		ArtifactsDependencyResolverImpl depResolver = new ArtifactsDependencyResolverImpl("target/maverepo", systemFactory);
		MavenArtifact mavenArtifactGpUi = MavenArtifact.builder().artifactId("gp-ui").groupId("com.affichage.it21.gp").version("9.1.0.ADMIN-UIMIG-SNAPSHOT").build();
		MavenArtifact mavenArtifactGpDao = MavenArtifact.builder().artifactId("gp-dao").groupId("com.affichage.it21.gp").version("9.1.0.ADMIN-UIMIG-SNAPSHOT").build();
		MavenArtifact mavenArtifactFakturaDao = MavenArtifact.builder().artifactId("faktura-dao").groupId("com.affichage.it21.vk").version("9.1.0.ADMIN-UIMIG-SNAPSHOT").build();
		ArrayList<MavenArtifact> artefacts = Lists.newArrayList(mavenArtifactGpUi,mavenArtifactGpDao,mavenArtifactFakturaDao);
		List<MavenArtWithDependencies> result = depResolver.resolveDependenciesInternal(artefacts);
		assert(result.size() == 3);
		MavenArtWithDependencies mavenArtGpUi = result.get(0);
		assertEquals(mavenArtifactGpUi, mavenArtGpUi.getArtifact());
		List<MavenArtifact> gpUiDependencies = mavenArtGpUi.getDependencies();
		assert(gpUiDependencies.size() == 1);
		assert(gpUiDependencies.contains(mavenArtifactGpDao)); 
		MavenArtWithDependencies mavenArtGpDao = result.get(1);
		List<MavenArtifact> gpDaoDependencies = mavenArtGpDao.getDependencies();
		assert(gpDaoDependencies.size() == 0);
		MavenArtWithDependencies mavenArtFakturaDao = result.get(2);
		assertEquals(mavenArtifactFakturaDao, mavenArtFakturaDao.getArtifact());
		List<MavenArtifact> fakturaDaoDependencies = mavenArtFakturaDao.getDependencies();
		assert(fakturaDaoDependencies.size() == 1);
		assert(gpUiDependencies.contains(mavenArtifactGpDao)); 
	}
	
	@Test
	public void testTwoArtifactsWithDependencyLevel() {
		ArtifactsDependencyResolverImpl depResolver = new ArtifactsDependencyResolverImpl("target/maverepo", systemFactory);
		MavenArtifact mavenArtifactGpUi =  MavenArtifact.builder().artifactId("gp-ui").groupId("com.affichage.it21.gp").version("9.1.0.ADMIN-UIMIG-SNAPSHOT").build();
		MavenArtifact mavenArtifactGpDao = MavenArtifact.builder().artifactId("gp-dao").groupId("com.affichage.it21.gp").version("9.1.0.ADMIN-UIMIG-SNAPSHOT").build();
		ArrayList<MavenArtifact> artefacts = Lists.newArrayList(mavenArtifactGpUi,mavenArtifactGpDao);
		depResolver.resolveDependencies(artefacts);
		artefacts.sort(Comparator.comparing(MavenArtifact::getDependencyLevel).reversed());
		artefacts.forEach(a -> System.out.println("Dependency Level: " + a.getDependencyLevel() + " for Artefact : " + a.toString()));
		assertEquals(Integer.valueOf(0),mavenArtifactGpUi.getDependencyLevel());
		assertEquals(Integer.valueOf(1),mavenArtifactGpDao.getDependencyLevel());
	}
	
	@Test
	public void testMoreArtifactsNestedDependenciesWithDependencyLevel() {
		ArtifactsDependencyResolverImpl depResolver = new ArtifactsDependencyResolverImpl("target/maverepo", systemFactory);
		MavenArtifact mavenArtifactGpUi = MavenArtifact.builder().artifactId("gp-ui").groupId("com.affichage.it21.gp").version("9.1.0.ADMIN-UIMIG-SNAPSHOT").build();
		MavenArtifact mavenArtifactGpDao = MavenArtifact.builder().artifactId("gp-dao").groupId("com.affichage.it21.gp").version("9.1.0.ADMIN-UIMIG-SNAPSHOT").build();
		MavenArtifact mavenArtifactFakturaDao = MavenArtifact.builder().artifactId("faktura-dao").groupId("com.affichage.it21.vk").version("9.1.0.ADMIN-UIMIG-SNAPSHOT").build();
		ArrayList<MavenArtifact> artefacts = Lists.newArrayList(mavenArtifactGpUi,mavenArtifactGpDao,mavenArtifactFakturaDao);
		depResolver.resolveDependencies(artefacts);
		artefacts.sort(Comparator.comparing(MavenArtifact::getDependencyLevel).reversed());
		artefacts.forEach(a -> System.out.println("Dependency Level: " + a.getDependencyLevel() + " for Artefact : " + a.toString()));
		assertEquals(Integer.valueOf(2),mavenArtifactGpDao.getDependencyLevel());
		assertEquals(Integer.valueOf(0),mavenArtifactGpUi.getDependencyLevel());
		assertEquals(Integer.valueOf(0),mavenArtifactFakturaDao.getDependencyLevel());


	}
	
	
	@Test
	public void testMegaPatchWithDependencyLevels() throws IOException {
		ArtifactsDependencyResolverImpl depResolver = new ArtifactsDependencyResolverImpl("target/maverepo", systemFactory);
		File patchFile = new File("src/test/resources/Patch5731.json");
		ObjectMapper mapper = new ObjectMapper();
		Patch patchData = mapper.readValue(patchFile, Patch.class);
		List<MavenArtifact> artefacts = patchData.retrieveAllArtifactsToPatch();
		depResolver.resolveDependencies(artefacts);
		artefacts.sort(Comparator.comparing(MavenArtifact::getDependencyLevel).reversed());
		artefacts.forEach(a -> System.out.println("Dependency Level: " + a.getDependencyLevel() + " for Artefact : " + a.toString()));
		File newPatchFile = new File("src/test/resources/Patch5731new.json");
		mapper.writeValue(new FileWriter(newPatchFile),patchData);
	}


}
