import org.springframework.core.io.FileSystemResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

def otherTargetInstances = ['CHTI211', 'CHTI213', 'CHTI214', 'CHQI211', 'CHPI211']
def targetSystems = []

targetSystems << new Expando(name:'Entwicklung', target:'CHEI212',
stages:[
	new Expando(name:'startPipelineAndTag',toState:'Installationsbereit',code:2, implcls:"com.apgsga.microservice.patch.server.impl.MockExecuteAction")
])
targetSystems << new Expando(name:'Informatiktest', target:'CHTI212',
stages:[
	new Expando(name:'buildAndAssemble',toState:'Installationsbereit',code:15,, implcls:"com.apgsga.microservice.patch.server.impl.MockExecuteAction"),
	new Expando(name:'install',toState:'',code:20, implcls:"com.apgsga.microservice.patch.server.impl.MockExecuteAction")
])
targetSystems << new Expando(name:'Produktion', target:'CHEI211',
stages:[
	new Expando(name:'buildAndAssemble',toState:'Installationsbereit',code:65, implcls:"com.apgsga.microservice.patch.server.impl.MockExecuteAction"),
	new Expando(name:'install',toState:'',code:80, implcls:"com.apgsga.microservice.patch.server.impl.MockExecuteAction")
])
def aggregateBean = new Expando(targetSystems:targetSystems,otherTargetInstances:otherTargetInstances)
def jsonTxt = JsonOutput.toJson(aggregateBean)
def jsonPretty = JsonOutput.prettyPrint(jsonTxt)
println jsonPretty
ResourceLoader rl = new FileSystemResourceLoader();
Resource parent = rl.getResource("src/test/resources")
def jsonFile = new File(parent.getFile(), "TargetSystemMappings.json")
jsonFile.withWriter('UTF-8') { writer ->
	writer.write(jsonPretty)
}
def json = new JsonSlurper().parseText(jsonTxt)
def stateMap = [:]
json.targetSystems.find( { a ->  a.stages.find( { stateMap.put("${a.name}${it.toState}","${it.code}") })} )
println stateMap








