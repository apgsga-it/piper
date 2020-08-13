import spock.lang.Specification

class TargetSystemMappingGroovyImplTest extends Specification {

    def tsmClassFilePath = "resources/targetSystemMappings.groovy"
    def tsmJson = "src/test/resources/TargetSystemMappings.json"
    def tsmClass

    def setup() {
        GroovyClassLoader gcl = new GroovyClassLoader()
        File f = new File(tsmClassFilePath)
        Class c = gcl.parseClass(f)
        tsmClass = c.create(tsmJson)
    }

    def "test serviceTypeFor"() {
        when:
            def linuxServiceType = tsmClass.serviceTypeFor("jadas","test-CHTI211")
            def guiServiceType = tsmClass.serviceTypeFor("it21_ui","test-CHEI211")
        then:
            linuxServiceType.equals("linuxservice")
            guiServiceType.equals("linuxbasedwindowsfilesystem")
    }

    def "test installTargetFor"() {
        when:
            def linuxServiceType = tsmClass.installTargetFor("jadas","test-CHTI211")
            def guiServiceType = tsmClass.installTargetFor("it21_ui","test-CHEI211")
        then:
            linuxServiceType == "test-jadas-t.apgsga.ch"
            guiServiceType == "test-service-chei211.apgsga.ch"
    }

    def "isLightInstance"() {
        when:
            def lightInstance = tsmClass.isLightInstance("test-dev-jhe")
            def nonLightInstance = tsmClass.isLightInstance("test-CHEI212")
        then:
            lightInstance == true
            nonLightInstance == false
    }
}