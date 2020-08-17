package com.apgsga.system.mappings

import com.apgsga.system.mapping.impl.TargetSystemMappingImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification

@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = TargetSystemMappingImpl.class)
class TargetSystemMappingTests extends Specification {

    @Autowired
    TargetSystemMappingImpl tsm

    def "test serviceTypeFor API"() {
        expect:
            tsm != null
            tsm.serviceTypeFor("jadas","test-CHTI211").equals("linuxservice")
            tsm.serviceTypeFor("it21_ui","test-CHEI211").equals("linuxbasedwindowsfilesystem")
    }

    def "test installTargetFor API"() {
        expect:
            tsm != null
            tsm.installTargetFor("jadas","test-CHTI211").equals("test-jadas-t.apgsga.ch")
            tsm.installTargetFor("it21_ui","test-CHEI211").equals("test-service-chei211.apgsga.ch")
    }

    def "test isLightInstance API"() {
        expect:
            tsm != null
            tsm.isLightInstance("test-dev-jhe")
            !tsm.isLightInstance("test-CHEI212")
    }

    def "test validToStates API"() {
        expect:
            tsm != null
            tsm.validToStates().size() == 8
            tsm.validToStates().contains("Entwicklung")
            tsm.validToStates().contains("Informatiktest")
            tsm.validToStates().contains("Anwendertest")
            tsm.validToStates().contains("Produktion")
            tsm.validToStates().contains("EntwicklungInstallationsbereit")
            tsm.validToStates().contains("InformatiktestInstallationsbereit")
            tsm.validToStates().contains("AnwendertestInstallationsbereit")
            tsm.validToStates().contains("ProduktionInstallationsbereit")
    }

    def "test listInstallTargets API"() {
        expect:
            tsm != null
            tsm.listInstallTargets().size() == 4
            tsm.listInstallTargets().contains("test-CHEI211")
            tsm.listInstallTargets().contains("test-CHEI212")
            tsm.listInstallTargets().contains("test-CHTI212")
            tsm.listInstallTargets().contains("test-devjhe")
    }
}
