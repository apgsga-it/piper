import com.apgsga.microservice.patch.api.TargetSystemEnvironments2

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



