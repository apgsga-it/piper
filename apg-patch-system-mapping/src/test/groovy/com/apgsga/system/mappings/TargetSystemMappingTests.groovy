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

    def "test stateMap API"() {
        expect:
            tsm != null
            def stateMap = tsm.stateMap()
            stateMap != null
            stateMap.size() == 8
            def keys = stateMap.keySet()
            keys.contains("Entwicklung")
            keys.contains("Informatiktest")
            keys.contains("Anwendertest")
            keys.contains("Produktion")
            keys.contains("EntwicklungInstallationsbereit")
            keys.contains("InformatiktestInstallationsbereit")
            keys.contains("AnwendertestInstallationsbereit")
            keys.contains("ProduktionInstallationsbereit")
            // Entwicklung entries
            stateMap.get("Entwicklung").get("targetName").equals("Entwicklung")
            stateMap.get("Entwicklung").get("clsName").equals("com.apgsga.microservice.patch.server.impl.PipelineInputAction")
            stateMap.get("Entwicklung").get("stage").equals("cancel")
            stateMap.get("Entwicklung").get("target").equals("test-CHEI212")
            // Informatiktest entries
            stateMap.get("Informatiktest").get("targetName").equals("Informatiktest")
            stateMap.get("Informatiktest").get("clsName").equals("com.apgsga.microservice.patch.server.impl.PipelineInputAction")
            stateMap.get("Informatiktest").get("stage").equals("InstallFor")
            stateMap.get("Informatiktest").get("target").equals("test-CHEI211")
            // Anwendertest entries
            stateMap.get("Anwendertest").get("targetName").equals("Anwendertest")
            stateMap.get("Anwendertest").get("clsName").equals("com.apgsga.microservice.patch.server.impl.PipelineInputAction")
            stateMap.get("Anwendertest").get("stage").equals("InstallFor")
            stateMap.get("Anwendertest").get("target").equals("test-CHTI211")
            // Produktion entries
            stateMap.get("Produktion").get("targetName").equals("Produktion")
            stateMap.get("Produktion").get("clsName").equals("com.apgsga.microservice.patch.server.impl.PipelineInputAction")
            stateMap.get("Produktion").get("stage").equals("InstallFor")
            stateMap.get("Produktion").get("target").equals("test-CHPI211")
            // EntwicklungInstallationsbereit entries
            stateMap.get("EntwicklungInstallationsbereit").get("targetName").equals("Entwicklung")
            stateMap.get("EntwicklungInstallationsbereit").get("clsName").equals("com.apgsga.microservice.patch.server.impl.EntwicklungInstallationsbereitAction")
            stateMap.get("EntwicklungInstallationsbereit").get("stage").equals("startPipelineAndTag")
            stateMap.get("EntwicklungInstallationsbereit").get("target").equals("test-CHEI212")
            // InformatiktestInstallationsbereit entries
            stateMap.get("InformatiktestInstallationsbereit").get("targetName").equals("Informatiktest")
            stateMap.get("InformatiktestInstallationsbereit").get("clsName").equals("com.apgsga.microservice.patch.server.impl.PipelineInputAction")
            stateMap.get("InformatiktestInstallationsbereit").get("stage").equals("BuildFor")
            stateMap.get("InformatiktestInstallationsbereit").get("target").equals("test-CHEI211")
            // AnwendertestInstallationsbereit entries
            stateMap.get("AnwendertestInstallationsbereit").get("targetName").equals("Anwendertest")
            stateMap.get("AnwendertestInstallationsbereit").get("clsName").equals("com.apgsga.microservice.patch.server.impl.PipelineInputAction")
            stateMap.get("AnwendertestInstallationsbereit").get("stage").equals("BuildFor")
            stateMap.get("AnwendertestInstallationsbereit").get("target").equals("test-CHTI211")
            // ProduktionInstallationsbereit entries
            stateMap.get("ProduktionInstallationsbereit").get("targetName").equals("Produktion")
            stateMap.get("ProduktionInstallationsbereit").get("clsName").equals("com.apgsga.microservice.patch.server.impl.PipelineInputAction")
            stateMap.get("ProduktionInstallationsbereit").get("stage").equals("BuildFor")
            stateMap.get("ProduktionInstallationsbereit").get("target").equals("test-CHPI211")

    }
}
