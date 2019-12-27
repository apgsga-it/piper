
import com.apgsga.microservice.patch.exceptions.Asserts
import com.apgsga.microservice.patch.exceptions.ExceptionFactory
import groovy.json.JsonSlurper
import org.springframework.core.io.FileSystemResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader

ResourceLoader rl = new FileSystemResourceLoader();
Resource parent = rl.getResource("$configDir")
Asserts.isTrue(parent.exists(),"Groovy.script.executePatchAction.configdir.exists.assert",[configDir,toState,patchNumber].toArray())
def jsonFile = new File(parent.getFile(), "${configFileName}")
Asserts.isTrue(jsonFile.exists(),"Groovy.script.executePatchAction.configfile.exists.assert",[configFileName,toState,patchNumber].toArray())
def json = new JsonSlurper().parseText(jsonFile.text)
def stateMap = [:]
json.stageMappings.find( { a ->  a.stages.find( { stateMap.put("${a.name}${it.toState}",new Expando(targetName:"${a.name}", clsName:"${it.implcls}",stage:"${it.name}",target:"${a.target}"))})} )
def bean = stateMap.get("${toState}")
Asserts.notNull(bean,"Groovy.script.executePatchAction.state.exits.assert",[toState,patchNumber].toArray())
println "Got bean : ${bean}"
println "Bean class name: ${bean.clsName} and ${patchContainerBean}"
def instance = this.class.classLoader.loadClass(bean.clsName).newInstance(patchContainerBean)
println "Done create instance ${instance}"
def parameter = [targetName:bean.targetName.toString(),target:bean.target.toString(),stage:bean.stage.toString()]
def msg = instance.executeToStateAction(patchNumber, toState, parameter)
println msg
return msg
