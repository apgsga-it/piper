package groovyScript

import com.apgsga.microservice.patch.exceptions.Asserts

def stateMap = targetSystemMapping.stateMap()
def bean = stateMap.get((String)"${toState}")
Asserts.notNull(bean,"Groovy.script.executePatchAction.state.exits.assert",[toState,patchNumber].toArray())
println "Got bean : ${bean}"
println "Bean class name: ${bean.clsName} and ${patchContainerBean}"
def instance = this.class.classLoader.loadClass(bean.clsName).newInstance(patchContainerBean)
println "Done create instance ${instance}"
def parameter = [targetName:bean.targetName.toString(),target:bean.target.toString(),stage:bean.stage.toString()]
def msg = instance.executeToStateAction(patchNumber, toState, parameter)
println msg
return msg
