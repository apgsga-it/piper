package com.apgsga.microservice.patch.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Lists
import spock.lang.Specification

class PatchServiceServiceMetaDataTests extends Specification {

    def servicesMetaData
    def patchService

    def setup() {
        final def builder = MavenArtifact.builder()
        def bomData = builder
                .artifactId("bomArtifactid")
                .groupId("bomGroupid")
                .name("whatevername")
                .build()
        def pkgData = Package.builder()
                .packagerName("packagermodulename")
                .starterCoordinates(Lists.newArrayList("someappgroupId:artifactId", "anotherappgroupId:anotherartifactId"))
                .build()
        def serviciceMetaData = ServiceMetaData.builder()
                .bomCoordinates(bomData)
                .baseVersionNumber("aBaseVersionNumber")
                .microServiceBranch("branch")
                .packages(Lists.newArrayList(pkgData))
                .revisionMnemoPart("revpart")
                .serviceName("testservice").build()
        servicesMetaData = ServicesMetaData.builder().servicesMetaData(Lists.newArrayList(serviciceMetaData)).build()
        patchService = Stub(PatchService)
    }

    def "Json Marshalling of ServicesMetaData"() {
        given:
        def om = new ObjectMapper()
        def jsonStr = om.writeValueAsString(servicesMetaData)
        when:
        def result = om.readValue(jsonStr,ServicesMetaData.class)
        then:
        result == servicesMetaData
    }

    def "PatchService all listServiceData"() {
        given:
        patchService.listServiceData() >> servicesMetaData.servicesMetaData

        when:
        def result = patchService.listServiceData()

        then:
        result == servicesMetaData.servicesMetaData
    }


}
