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

    def "test TargetSystemMapping API"() {
        expect:
            tsm != null
            tsm.serviceTypeFor("jadas","test-CHTI211").equals("linuxservice")
            tsm.serviceTypeFor("it21_ui","test-CHEI211").equals("linuxbasedwindowsfilesystem")
            tsm.installTargetFor("jadas","test-CHTI211").equals("test-jadas-t.apgsga.ch")
            tsm.installTargetFor("it21_ui","test-CHEI211").equals("test-service-chei211.apgsga.ch")
            tsm.isLightInstance("test-dev-jhe")
            !tsm.isLightInstance("test-CHEI212")
    }
}
