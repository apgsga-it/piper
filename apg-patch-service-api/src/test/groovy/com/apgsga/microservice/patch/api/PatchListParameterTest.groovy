package com.apgsga.microservice.patch.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Lists
import org.spockframework.util.Assert
import spock.lang.Specification

class PatchListParameterTest extends Specification {

    def "test JSON marshalling"() {
        given:
            def emailsFor1234 = Lists.newArrayList("paul@apgsga.ch","bernard@apgsga.ch")
            def pl = PatchListParameter.builder().patchNumber("1234").emails(emailsFor1234).build()
            def emailsFor5678 = Lists.newArrayList("jeff@apgsga.ch")
            def pl2 = PatchListParameter.builder().patchNumber("5678").emails(emailsFor5678).build()
            List<PatchListParameter> pls = Lists.newArrayList(pl,pl2)
        when:
            ObjectMapper om = new ObjectMapper()
            def plAsJson = om.writeValueAsString(pls)
        then:
            plAsJson != null
            println(plAsJson)
    }

    def "test JSON unmarshalling"() {
        given:
            def jsonString = '[{"patchNumber":"1234","eMails": ["paul@apgsga.ch","robert@apgsga.ch"]},{"patchNumber":"5678","eMails": ["jeff@apgsga.ch"]}]'
        when:
            def om = new ObjectMapper()
            def parameters = om.readValue(jsonString, PatchListParameter[].class)
        then:
            parameters.size() == 2
            parameters.each {patchListParam ->
                if(patchListParam.getPatchNumber().equals("1234")) {
                    patchListParam.getEmails.size() == 2
                    patchListParam.getEmails.contains("paul@apgsga.ch")
                    patchListParam.getEmails.contains("robert@apgsga.ch")
                }
                else if(patchListParam.getPatchNumber().equals("5678")) {
                    patchListParam.getEmails.size() == 1
                    patchListParam.getEmails.contains("jeff@apgsga.ch")
                }
                else {
                    Assert.fail("patchNumber should not be anything else than 1234 or 5678", patchListParam)
                }
            }
    }
}
