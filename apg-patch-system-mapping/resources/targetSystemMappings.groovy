import groovy.json.JsonSlurper
import org.springframework.util.Assert

import java.nio.file.Files

def static create(def tsmFilePath) {
    File tsmFile = new File(tsmFilePath)
    Assert.isTrue(Files.exists(tsmFile.toPath()),"${tsmFilePath} does not exist!!")
    def tsmInstance = TargetSystemMappings.instance
    tsmInstance.setTsmFile(tsmFile)
    tsmInstance
}

@Singleton()
class TargetSystemMappings {

    private File tsmFile
    def setTsmFile(def tsmFile){
        this.tsmFile = tsmFile
    }

    def findStatus(String toStatus) {
        //TODO JHE, not sure what we want here
    }

    def serviceTypeFor(String serviceName, String target) {
        def targetInstances = loadTargetInstances(tsmFile.text)
        return targetInstances."${target}".find{service -> service.name.equalsIgnoreCase(serviceName)}.type
    }

    def installTargetFor(String serviceName, String target) {
        def targetInstances = loadTargetInstances(tsmFile.text)
        return targetInstances."${target}".find{service -> service.name.equalsIgnoreCase(serviceName)}.host
    }

    def isLightInstance(String target) {
        def targetInstances = loadTargetInstances(tsmFile.text)
        // JHE (12.08.2020): For now, we check if "light" is in host name of the DB service
        return targetInstances."${target}".find{service -> service.name.equalsIgnoreCase("it21-db")}.host.contains("light")
    }

    private def loadTargetInstances(targetSystemMappingAsText) {
        def targetInstances = [:]
        def targetSystemMappingAsJson = new JsonSlurper().parseText(targetSystemMappingAsText)
        targetSystemMappingAsJson.targetInstances.each( {targetInstance ->
            targetInstances.put(targetInstance.name,targetInstance.services)
        })
        targetInstances
    }
}