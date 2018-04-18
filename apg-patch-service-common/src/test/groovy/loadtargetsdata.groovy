import org.springframework.core.io.FileSystemResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader

import com.apgsga.microservice.patch.api.TargetSystemEnvironments2

import groovy.json.JsonOutput

def logicalNameInstanceMap = [Entwicklung:'CHEI212', Informatiktest:'CHTI212', Produktion:'CHEI211']
def otherTargetInstances = [
	'CHTI211',
	'CHTI213',
	'CHTI214',
	'CHQI211',
	'CHPI211'
]
def stateMap = [EntwicklungInstallationsbereit:2,InformatiktestInstallationsbereit:15, ProduktionInstallationsbereit:65, Entwicklung:0, Informatiktest:20, Produktion:80]
def targetSystemData = new TargetSystemEnvironments2();
targetSystemData.logicalNameInstanceMap = logicalNameInstanceMap
targetSystemData.otherInstances = otherTargetInstances
targetSystemData.stateMap = stateMap
def jsonOutput = JsonOutput.prettyPrint(JsonOutput.toJson(targetSystemData))
println jsonOutput
ResourceLoader rl = new FileSystemResourceLoader();
Resource parent = rl.getResource("src/test/resources")
def outputFile = new File(parent.getFile(), "TargetSystemMappings.json")
outputFile.withWriter('UTF-8') { writer ->
	writer.write(jsonOutput)
}

