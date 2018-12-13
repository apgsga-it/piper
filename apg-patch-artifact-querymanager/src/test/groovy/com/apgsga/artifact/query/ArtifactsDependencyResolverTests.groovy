package com.apgsga.artifact.query
import java.util.ArrayList
import java.util.Comparator
import java.util.function.Function

import org.springframework.beans.factory.annotation.Value
import org.springframework.test.context.ContextConfiguration

import com.apgsga.artifact.query.impl.ArtifactsDependencyResolverImpl
import com.apgsga.microservice.patch.api.MavenArtifact
import com.apgsga.microservice.patch.api.impl.MavenArtifactBean
import com.apgsga.test.config.TestConfig
import com.google.common.collect.Lists
import static java.util.stream.Collectors.groupingBy;


import spock.lang.Specification;

@ContextConfiguration(classes = TestConfig.class)
class ArtifactsDependencyResolverTests extends Specification {
	
	@Value('${mavenrepo.user.name}')
	def repoUser
	
	@Value('${mavenrepo.baseurl}')
	def repoUrl
	
	@Value('${mavenrepo.name}')
	def repoName
	
	@Value('${mavenrepo.user.encryptedPwd}')
	def mavenRepoUserEncryptedPwd
	
	@Value('${mavenrepo.user.decryptpwd.key:}')
	def mavenRepoUserDecryptKey;

	
	def systemFactory
	
	def setup() {
		systemFactory = RepositorySystemFactory.create(repoUrl, repoName, repoUser, mavenRepoUserEncryptedPwd,mavenRepoUserDecryptKey);
	}

	def "Collect Artefacts by Dependencylevel"() {
		setup:
		def depResolver = new ArtifactsDependencyResolverImpl("target/maverepo", systemFactory)
		def mavenArtifactGpUi = new MavenArtifactBean("gp-ui","com.affichage.it21.gp","9.1.0.ADMIN-UIMIG-SNAPSHOT")
		def mavenArtifactGpDao = new MavenArtifactBean("gp-dao","com.affichage.it21.gp","9.1.0.ADMIN-UIMIG-SNAPSHOT")
		def mavenArtifactFakturaDao = new MavenArtifactBean("faktura-dao","com.affichage.it21.vk","9.1.0.ADMIN-UIMIG-SNAPSHOT")
		def artefacts = Lists.newArrayList(mavenArtifactGpUi,mavenArtifactGpDao,mavenArtifactFakturaDao)
		when:
		depResolver.resolveDependencies(artefacts);
		artefacts.sort new OrderBy([{it.dependencyLevel}])
		artefacts.reverse()
		artefacts.each { 
			println "Dependency Level: ${it.dependencyLevel} for Artefact: ${it.toString()}"
		}		
		def splitLists = artefacts.groupBy {
			it.dependencyLevel
		}		
		println splitLists.toString()
		then:
		def artsLevelZero = splitLists[0]
		assert artsLevelZero.size() == 2
		assert artsLevelZero.contains(mavenArtifactGpUi)
		assert artsLevelZero.contains(mavenArtifactFakturaDao)
		def artsLevel2 = splitLists[2]
		assert artsLevel2.size() == 1
		assert artsLevel2.contains(mavenArtifactGpDao)	
	}
	
	def "Collect and Process Artefacts by Dependencylevel"() {
		setup:
		def depResolver = new ArtifactsDependencyResolverImpl("target/maverepo", systemFactory)
		def mavenArtifactGpUi = new MavenArtifactBean("gp-ui","com.affichage.it21.gp","9.1.0.ADMIN-UIMIG-SNAPSHOT")
		def mavenArtifactGpDao = new MavenArtifactBean("gp-dao","com.affichage.it21.gp","9.1.0.ADMIN-UIMIG-SNAPSHOT")
		def mavenArtifactFakturaDao = new MavenArtifactBean("faktura-dao","com.affichage.it21.vk","9.1.0.ADMIN-UIMIG-SNAPSHOT")
		def artefacts = Lists.newArrayList(mavenArtifactGpUi,mavenArtifactGpDao,mavenArtifactFakturaDao)
		when:
		depResolver.resolveDependencies(artefacts);
		artefacts.sort new OrderBy([{it.dependencyLevel}])
		def listsByDepLevel = artefacts.stream().collect(groupingBy((Function) {  b -> return b.dependencyLevel }))
		def depLevels = listsByDepLevel.keySet() as List
		depLevels.sort()
		depLevels.reverse(true)
		def depLevelIterator = depLevels.iterator()
		def level2 = depLevelIterator.next()
		def level0 = depLevelIterator.next()
		then:
		assert !depLevelIterator.hasNext()
		assert listsByDepLevel[level2].size() == 1
		assert listsByDepLevel[level2].contains(mavenArtifactGpDao)
		assert listsByDepLevel[level0].size() == 2
		assert listsByDepLevel[level0].contains(mavenArtifactGpUi)
		assert listsByDepLevel[level0].contains(mavenArtifactFakturaDao)
		
		
		
	}
}
