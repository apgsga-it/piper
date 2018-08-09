package com.apgsga.artifact.query
import java.util.ArrayList
import java.util.Comparator
import java.util.function.Function
import com.apgsga.artifact.query.impl.ArtifactsDependencyResolverImpl
import com.apgsga.microservice.patch.api.MavenArtifact
import com.apgsga.microservice.patch.api.impl.MavenArtifactBean
import com.google.common.collect.Lists
import static java.util.stream.Collectors.groupingBy;


import spock.lang.Specification;

class ArtifactsDependencyResolverTests extends Specification {

	def "Splitting up and Ordering by Dependencylevel"() {
		setup:
		def depResolver = new ArtifactsDependencyResolverImpl("target/maverepo")
		def mavenArtifactDt = new MavenArtifactBean("datetime-utils","com.affichage.datetime","9.0.6.ADMIN-UIMIG-SNAPSHOT")
		def mavenArtifactGpUi = new MavenArtifactBean("gp-ui","com.affichage.it21.gp","9.0.6.ADMIN-UIMIG-SNAPSHOT")
		def mavenArtifactGpDao = new MavenArtifactBean("gp-dao","com.affichage.it21.gp","9.0.6.ADMIN-UIMIG-SNAPSHOT")
		def mavenArtifactFakturaDao = new MavenArtifactBean("faktura-dao","com.affichage.it21.vk","9.0.6.ADMIN-UIMIG-SNAPSHOT")
		def artefacts = Lists.newArrayList(mavenArtifactGpUi,mavenArtifactGpDao,mavenArtifactDt,mavenArtifactFakturaDao)
		when:
		depResolver.resolveDependencies(artefacts);
		artefacts.sort new OrderBy([{it.dependencyLevel}])
		artefacts.reverse()
		artefacts.each { 
			println "Dependency Level: ${it.dependencyLevel} for Artefact: ${it.toString()}"
		}		
		def splitLists = artefacts.stream().collect(groupingBy((Function) { MavenArtifactBean b -> return b.dependencyLevel }))
		println splitLists.toString()
		then:
		def artsLevelZero = splitLists[0]
		assert artsLevelZero.size() == 2
		assert artsLevelZero.contains(mavenArtifactGpUi)
		assert artsLevelZero.contains(mavenArtifactFakturaDao)
		def artsLevel2 = splitLists[2]
		assert artsLevel2.size() == 1
		assert artsLevel2.contains(mavenArtifactGpDao)
		def artsLevel3 = splitLists[3]
		assert artsLevel3.size() == 1
		assert artsLevel3.contains(mavenArtifactDt)
		
	}
}
