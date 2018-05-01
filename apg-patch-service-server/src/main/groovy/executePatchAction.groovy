
import groovy.json.JsonSlurper

import java.lang.reflect.Constructor

import org.springframework.beans.BeanUtils 
import org.springframework.core.io.FileSystemResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader

ResourceLoader rl = new FileSystemResourceLoader();
Resource parent = rl.getResource("$configDir")
def jsonFile = new File(parent.getFile(), "${configFileName}")
def json = new JsonSlurper().parseText(jsonFile.text)
def stateMap = [:]
json.targetSystems.find( { a ->  a.stages.find( { stateMap.put("${a.name}${it.toState}",new Expando(targetName:"${a.name}", clsName:"${it.implcls}",stage:"${it.name}",target:"${a.target}"))})} )
def bean = stateMap.get("${toState}")
if (bean == null) {
	throw new RuntimeException("No Valid toState: ${toState}")
}
println "Got bean : ${bean}"
println "Bean class name: ${bean.clsName} and ${patchContainerBean}"
def instance = this.class.classLoader.loadClass(bean.clsName).newInstance(patchContainerBean)
println "Done create instance ${instance}"
def parameter = [targetName:bean.targetName.toString(),target:bean.target.toString(),stage:bean.stage.toString()]
def msg = instance.executeToStateAction(patchNumber, toState, parameter)
println msg
return msg
