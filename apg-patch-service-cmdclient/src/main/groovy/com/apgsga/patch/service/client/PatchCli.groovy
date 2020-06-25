package com.apgsga.patch.service.client

import com.apgsga.patch.service.client.config.PliConfig
import com.apgsga.patch.service.client.rest.PatchRestServiceClient
import org.codehaus.groovy.runtime.StackTraceUtils
import com.apgsga.microservice.patch.api.DbModules
import com.apgsga.microservice.patch.api.Patch
import com.apgsga.microservice.patch.api.ServicesMetaData
import com.apgsga.patch.service.client.utils.TargetSystemMappings
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.AnnotationConfigApplicationContext

class PatchCli {

	static PatchCli create() {
		def patchCli = new PatchCli()
		return patchCli
	}

	def validComponents = [ "aps", "nil"]
	def validate = true
	def config

	private PatchCli() {
	}

	def process(def args) {
		def context =  new AnnotationConfigApplicationContext(PliConfig.class);
		config = context.getBean(ConfigObject.class);
		TargetSystemMappings.instance.load(config)
		def cmdResults = new Expando();
		cmdResults.returnCode = 1
		cmdResults.results = [:]
		def options = validateOpts(args)
		if (!options) {
			cmdResults.returnCode = 0
			return cmdResults
		}
		try {
			def patchClient = new PatchRestServiceClient(config)
			if (options.e) {
				def result = patchExists(patchClient,options)
				cmdResults.results['e'] =  result
			}
			if (options.fs) {
				def result = findById(patchClient,options)
				cmdResults.results['f'] = result
			}
			if (options.sa) {
				def result = savePatch(patchClient,options)
				cmdResults.results['sa']=  result
			}
			if (options.sta) {
				def result = stateChangeAction(patchClient,options)
				cmdResults.results['sta'] = result
			}
			if (options.cm) {
				def result = cleanLocalMavenRepo(patchClient)
				cmdResults.results['cm'] = result
			}
			if (options.log) {
				def result = logPatchActivity(patchClient,options)
				cmdResults.results['log'] = result
			}
			if (options.adp) {
				def result = assembleAndDeployPipeline(patchClient,options)
				cmdResults.results['adp'] = result
			}
			cmdResults.returnCode = 0
			return cmdResults
		} catch (PatchClientServerException e) {
			System.err.println "Server Error ccurred on ${e.errorMessage.timestamp} : ${e.errorMessage.errorText} "
			cmdResults.results['error'] = e.errorMessage
			return cmdResults
		} catch (AssertionError e) {
			System.err.println "Client Error ccurred ${e.message} "
			cmdResults.results['error'] = e.message
			return cmdResults
		} catch (Exception e) {
			System.err.println " Unhandling Exception occurred "
			System.err.println e.toString()
			StackTraceUtils.printSanitizedStackTrace(e,new PrintWriter(System.err))
			cmdResults.results['error'] = e
			return cmdResults
		}
	}

	def validateOpts(args) {
		def cli = new CliBuilder(usage: 'apspli.sh [-u <url>] [-h] [-[l|d|dd|dm] <directory>]  [-[e|r] <patchnumber>] [-[s|sa|ud|um] <file>] [-f <patchnumber,directory>] [-sta <patchnumber,toState,[aps]]')
		cli.formatter.setDescPadding(0)
		cli.formatter.setLeftPadding(0)
		cli.formatter.setWidth(100)
		cli.with {
			h longOpt: 'help', 'Show usage information', required: false
			e longOpt: "exists", args:1, argName: 'patchNumber', 'True resp. false if Patch of the <patchNumber> exists or not', required: false
			f longOpt: 'findById', args:2, valueSeparator: ",", argName: 'patchNumber,directory','Retrieve a Patch with the <patchNumber> to a <directory>', required: false
			sa longOpt: 'save', args:1, argName: 'patchFile', 'Saves a <patchFile> to the server, which starts the Patch Pipeline', required: false
			// TODO (CHE,13.9) Get rid of the component parameter, needs to be coordinated with current Patch System (PatchOMat)
			sta longOpt: 'stateChange', args:3, valueSeparator: ",", argName: 'patchNumber,toState,component', 'Notfiy State Change for a Patch with <patchNumber> to <toState> to a <component> , where <component> can only be aps ', required: false
			cm longOpt: 'cleanLocalMavenRepo', "Clean local Maven Repo used bei service", required: false
			log longOpt: 'log', args:1, argName: 'patchFile', 'Log a patch steps for a patch', required: false
			adp longOpt: 'assembleDeployPipeline', args:1, argName: 'target', "start an assembleAndDeploy pipeline for the given target", required: false
		}

		def options = cli.parse(args)
		def error = false;

		if (options == null) {
			println "Wrong parameters"
			cli.usage()
			return null
		}

		if (!validate) {
			return options
		}

		if (!options | options.h| options.getOptions().size() == 0) {
			cli.usage()
			def validToStates = TargetSystemMappings.instance.get().keySet()
			println "Valid toStates are: ${validToStates}"
			println "Valid components are: ${validComponents}"
			return null
		}
		if (options.fs) {
			def patchNumber = options.fs[0]
			if (!patchNumber.isInteger()) {
				println "Patchnumber ${patchNumber} is not a Integer"
				error = true
			}
			def dirName = options.fs[1]
			def directory = new File(dirName)
			if (!directory.exists() | !directory.directory) {
				println "Directory ${dirName} not valid: either not a directory or it doesn't exist"
				error = true
			}
		}
		if (options.sa) {
			def patchFile = new File(options.sa)
			if (!patchFile.exists() | !patchFile.file) {
				println "Patch File ${options.sa} not valid: either not a file or it doesn't exist"
				error = true
			}
		}

		if (options.sta) {
			if (options.stas.size() != 3 ) {
				println "Option sta needs 3 arguments: <patchNumber,toState, aps]>"
				error = true
			}
			def patchNumber = options.stas[0]
			if (!patchNumber.isInteger()) {
				println "Patchnumber ${patchNumber} is not a Integer"
				error = true
			}
			def toState = options.stas[1]
			def validToStates = TargetSystemMappings.instance.get().keySet()
			if (!validToStates.contains(toState) ) {
				println "ToState ${toState} not valid: needs to be one of ${validToStates}"
				error = true
			}
			def component = options.stas[2]
			if (component != null && !validComponents.contains(component.toLowerCase()) ) {
				println "Component ${component} not valid: needs to be one of ${validComponents}"
				error = true
			}
		}
		if (options.e) {
			if (!options.e.isInteger()) {
				println "Patchnumber ${options.e} is not a Integer"
				error = true
			}
		}
		if (options.cr) {
			if(options.crs.size() != 1) {
				println "target parameter is required when cleaning Artifactory releases."
				error = true
			}
		}
		if (options.log) {
			def patchFile = new File(options.log)
			if (!patchFile.exists() | !patchFile.file) {
				println "Patch File ${options.log} not valid: either not a file or it doesn't exist"
				error = true
			}
		}
		if (options.adp) {
			if(options.adps.size() != 1) {
				println "target parameter is required."
				error = true
			}
		}
		if (error) {
			cli.usage()
			return null
		}
		options
	}

	def cleanLocalMavenRepo(def patchClient) {
		def cmdResult = new Expando()
		patchClient.cleanLocalMavenRepo();
		cmdResult
	}


	def stateChangeAction(def patchClient,def options) {
		def cmdResult = new Expando()
		def patchNumber = options.stas[0]
		def toState = options.stas[1]
		def component = options.stas[2].toLowerCase()
		cmdResult.patchNumber = patchNumber
		cmdResult.toState = toState
		cmdResult.component = component
		if (component.equals("aps")) {
			patchClient.executeStateTransitionAction(patchNumber,toState)
		} else {
			println "Skipping State change Processing for ${patchNumber}"
		}
		return cmdResult
	}

	def findById(def patchClient,def options) {
		def cmdResult = new Expando()
		def patchNumber = options.fs[0]
		def dirName = options.fs[1]
		def found = retrieveAndWritePatch(patchClient, patchNumber, dirName )
		cmdResult.patchNumber = patchNumber
		cmdResult.dirName = dirName
		cmdResult.exists = found
		return cmdResult
	}

	def patchExists(def patchClient,def options) {
		def exists = patchClient.patchExists(options.e)
		println "Patch ${options.e} exists is: ${exists} "
		def cmdResult = new Expando()
		cmdResult.exists = exists
		return cmdResult
	}

	def savePatch(def patchClient,def options) {
		patchClient.save(new File(options.sa), Patch.class)
		def cmdResult = new Expando()
		cmdResult.patchFile = options.sa
		return cmdResult
	}
	
	def retrieveAndWritePatch(def patchClient,def id, def file) {
		println "Writting: ${id} to ${file}"
		def patchData = patchClient.findById(id)
		if (patchData == null) {
			return false
		}
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(new File(file,"Patch" + id + ".json"), patchData)
		println "Writting: ${id} to ${file} done."
		return true
	}

	def logPatchActivity(def patchClient,def options) {
		println "Logging patch activity for ${options.logs[0]}"
		ObjectMapper mapper = new ObjectMapper();
		def patchFile = mapper.readValue(new File("${options.logs[0]}"), Patch.class)
		patchClient.savePatchLog(patchFile)
	}

	def assembleAndDeployPipeline(def patchClient, def options) {
		def target = options.adps[0]
		println "Starting assembleAndDeploy pipeline for ${target}"
		patchClient.startAssembleAndDeployPipeline(target)
	}
}