import groovy.json.JsonSlurper
import org.springframework.core.io.FileSystemResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader

ResourceLoader rl = new FileSystemResourceLoader();
Resource parent = rl.getResource("src/test/resources/json")
def jsonFile = new File(parent.getFile(), "TargetSystemMappingsMock.json")
def json = new JsonSlurper().parseText(jsonFile.text)
def parameter = ['someparameter': 'ParameterValue', 'another': 'AnotherValue']
def stateMap = [:]
json.stageMappings.find({ a -> a.stages.find({ stateMap.put("${a.name}${it.toState}", "${it.implcls}") }) })
stateMap.keySet().forEach({
    def clx = stateMap.get("${it}")
    def instance = this.class.classLoader.loadClass(clx).newInstance()
    println instance.executeToStateAction("9995", "Somestate", parameter)
})
println stateMap
return stateMap