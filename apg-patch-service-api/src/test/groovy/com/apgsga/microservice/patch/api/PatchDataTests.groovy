package com.apgsga.microservice.patch.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Lists
import spock.lang.Specification

class PatchDataTests extends Specification  {
    def "Json Marshalling of Patch"() {
        given:

        def DBPatch dbPatch = DBPatch.builder().dbPatchBranch("db_patch_branch").prodBranch("prod_branch").build()
        dbPatch.addDbObject(DbObject.builder().
                fileName("somefileName").
                filePath("a/b/c").moduleName("Dboject1").build())
        dbPatch.addDbObject(DbObject.builder().
                fileName("anotherFile").
                filePath("a/b/d").moduleName("Dboject2").build())

        def patch = Patch.builder().patchNumber("9300")
        .dbPatch(dbPatch)
        .developerBranch("developer_branch")
        .dockerServices(Lists.newArrayList("dockerService1", "dockerService2", "dockerServices3"))
        .services(Lists.newArrayList(
                Service.builder().serviceName("aService").
                artifactsToPatch(Lists.newArrayList(
                        MavenArtifact.builder().artifactId("someId").groupId("grpId").version("1.0").build(),
                        MavenArtifact.builder().artifactId("anotherId").groupId("anotherGrpId").version("1.1").build()
                )).build()
        )).build()
        def om = new ObjectMapper()
        File output = new File("build/PatchTest.json")
        om.writeValue(output, patch)

        when:
        def result = om.readValue(output,Patch.class)
        then:
        result == patch
    }
}
