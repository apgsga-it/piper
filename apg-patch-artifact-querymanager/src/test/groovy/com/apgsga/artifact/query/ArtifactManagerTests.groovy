package com.apgsga.artifact.query
import java.util.ArrayList
import java.util.Comparator
import java.util.function.Function
import com.apgsga.artifact.query.impl.ArtifactsDependencyResolverImpl
import com.apgsga.microservice.patch.api.MavenArtifact
import com.apgsga.microservice.patch.api.SearchFilter
import com.apgsga.microservice.patch.api.impl.MavenArtifactBean
import com.google.common.collect.Lists
import static java.util.stream.Collectors.groupingBy;


import spock.lang.Specification;

class ArtifactManagerTests extends Specification {

	def "Default Filter Selection of Artifacts"() {
		setup:
		def artifactManager = ArtifactManager.create("com.affichage.common.maven","dm-bom","target/maverepo")
		when:
		def results = artifactManager.getAllDependencies("9.0.6.ADMIN-UIMIG-SNAPSHOT")
		def nonApgResults = results.findAll{ (!it.groupId.startsWith("com.apgsga") && ! it.groupId.startsWith("com.affichage"))}
		then:
		assert results.size() > 0
		assert nonApgResults.size() == 0
		
	}
	
	def "With Explicit Default Filter Selection of Artifacts"() {
		setup:
		def artifactManager = ArtifactManager.create("com.affichage.common.maven","dm-bom","target/maverepo")
		when:
		def results = artifactManager.getAllDependencies("9.0.6.ADMIN-UIMIG-SNAPSHOT",SearchFilter.DEFAULT)
		def nonApgResults = results.findAll{ (!it.groupId.startsWith("com.apgsga") && ! it.groupId.startsWith("com.affichage"))}
		then:
		assert results.size() > 0
		assert nonApgResults.size() == 0
		
	}
	

	
	def "With All Filter Selection of Artifacts"() {
		setup:
		def artifactManager = ArtifactManager.create("com.affichage.common.maven","dm-bom","target/maverepo")
		when:
		def results = artifactManager.getAllDependencies("9.0.6.ADMIN-UIMIG-SNAPSHOT",SearchFilter.ALL)
		println results.size()
		def nonApplicationResults = results.findAll{ (!it.groupId.startsWith("com.apgsga") && ! it.groupId.startsWith("com.affichage"))}
		def applicationResults = results.findAll{ (it.groupId.startsWith("com.apgsga") ||  it.groupId.startsWith("com.affichage"))}
		
		then:
		assert results.size() > 0
		assert nonApplicationResults.size() > 0
		assert applicationResults.size() > 0
		
		
	}
	
	def "With Application Filter Selection of Artifacts"() {
		setup:
		def artifactManager = ArtifactManager.create("com.affichage.common.maven","dm-bom","target/maverepo")
		when:
		def results = artifactManager.getAllDependencies("9.0.6.ADMIN-UIMIG-SNAPSHOT",SearchFilter.APPLICATION)
		def nonApgResults = results.findAll{ (!it.groupId.startsWith("com.apgsga") && ! it.groupId.startsWith("com.affichage"))}
		then:
		assert results.size() > 0
		assert nonApgResults.size() == 0
		
	}
	
}
