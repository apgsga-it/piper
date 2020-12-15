package com.apgsga.microservice.patch.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Lists
import spock.lang.Specification
import sun.security.util.ManifestEntryVerifier

class PatchDataTests extends Specification  {
    def "Json Marshalling of Patch"() {
        given:

        def patch = Patch.builder().patchNumber("9300")
        .dbObjects(Lists.newArrayList(
                DbObject.builder().
                        fileName("somefileName").
                        filePath("a/b/c").moduleName("Dboject1").build(),
                DbObject.builder().
                        fileName("anotherFile").
                        filePath("a/b/d").moduleName("Dboject2").build(),
        ))
        .dbPatchBranch("db_patch_branch")
        .developerBranch("developer_branch")
        .dockerServices(Lists.newArrayList("dockerService1", "dockerService2", "dockerServices3"))
        .prodBranch("prod_branch")
        .services(Lists.newArrayList(
                Service.builder().serviceName("aService").
                artifactsToPatch(Lists.newArrayList(
                        MavenArtifact.builder().artifactId("someId").groupId("grpId").version("1.0").build(),
                        MavenArtifact.builder().artifactId("anotherId").groupId("anotherGrpId").version("1.1").build()
                )).build()
        )).build();
        def om = new ObjectMapper()
        File output = new File("build/PatchTest.json")
        om.writeValue(output, patch)

        when:
        def result = om.readValue(output,Patch.class)
        then:
        result == patch
    }
}
