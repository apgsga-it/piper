package com.apgsga.artifact.query
import com.apgsga.microservice.patch.api.SearchCondition

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



	def "With All Filter Selection of Artifacts"() {
		setup:
		def artifactManager = ArtifactManager.create("com.affichage.common.maven","dm-bom","target/maverepo")
		when:
		def results = artifactManager.getAllDependencies("9.0.6.ADMIN-UIMIG-SNAPSHOT",SearchCondition.ALL)
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
		def results = artifactManager.getAllDependencies("9.0.6.ADMIN-UIMIG-SNAPSHOT",SearchCondition.APPLICATION)
		def nonApgResults = results.findAll{ (!it.groupId.startsWith("com.apgsga") && ! it.groupId.startsWith("com.affichage"))}
		then:
		assert results.size() > 0
		assert nonApgResults.size() == 0
	}
}
