package com.apgsga.patch.service.client

import com.apgsga.microservice.patch.api.Patch
import com.apgsga.patch.service.client.config.PliConfig
import com.apgsga.patch.service.client.rest.PatchRestServiceClient
import com.apgsga.patch.service.client.serverless.PatchServerlessImpl
import com.apgsga.patch.service.client.utils.TargetSystemMappings
import com.fasterxml.jackson.databind.ObjectMapper
import org.codehaus.groovy.runtime.StackTraceUtils
import org.springframework.context.annotation.AnnotationConfigApplicationContext

class PatchCli {

	static PatchCli create(client) {
		def patchCli = new PatchCli(client)
		return patchCli
	}

	def validComponents = [ "aps", "nil"]
	def validate = true
	def config
	def client

	private PatchCli(client) {
	  this.client = client
	}


	def process(def args) {
		def context =  new AnnotationConfigApplicationContext(PliConfig.class)
		config = context.getBean(ConfigObject.class)
		TargetSystemMappings.instance.load(config)
		def cmdResults = new Expando()
		cmdResults.returnCode = 1
		cmdResults.results = [:]
		def options = validateOpts(args)
		if (!options) {
			cmdResults.returnCode = 0
			return cmdResults
		}
		try {
			def patchClient = client == "pliLess" ? context.getBean(PatchServerlessImpl.class) : new PatchRestServiceClient(config)
			if (options.sa) {
				def result = savePatch(patchClient,options)
				cmdResults.results['sa']=  result
			}
			if (options.sta) {
				def result = stateChangeAction(patchClient,options)
				cmdResults.results['sta'] = result
			}
			if (options.oc) {
				def result = onClone(patchClient,options)
				cmdResults.results['oc'] = result
			}
			if (options.cm) {
				def result = cleanLocalMavenRepo(patchClient)
				cmdResults.results['cm'] = result
			}
			if (options.log) {
				def result = logPatchActivity(patchClient,options)
				cmdResults.results['log'] = result
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
		def cli = new CliBuilder(usage: 'apspli.sh [-u <url>] [-h]  [-[s|sa <file>] [-sta <patchnumber,toState,[aps]]')
		cli.formatter.setDescPadding(0)
		cli.formatter.setLeftPadding(0)
		cli.formatter.setWidth(100)
		cli.with {
			h longOpt: 'help', 'Show usage information', required: false
			s longOpt: 'save', args:1, argName: 'patchFile', 'Uploads a <patchFile> to the server', required: false
			sa longOpt: 'save', args:1, argName: 'patchFile', 'Saves a <patchFile> to the server, which starts the Patch Pipeline', required: false
			// TODO (CHE,13.9) Get rid of the component parameter, needs to be coordinated with current Patch System (PatchOMat)
			sta longOpt: 'stateChange', args:3, valueSeparator: ",", argName: 'patchNumber,toState,component', 'Notfiy State Change for a Patch with <patchNumber> to <toState> to a <component> , where <component> can only be aps ', required: false
			oc longOpt: 'onclone', args:2, valueSeparator: ",", argName: 'source,target', 'Call Patch Service onClone REST API', required: false
			cm longOpt: 'cleanLocalMavenRepo', "Clean local Maven Repo used bei service", required: false
			log longOpt: 'log', args:1, argName: 'patchFile', 'Log a patch steps for a patch', required: false
		}

		def options = cli.parse(args)
		def error = false

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
		if (options.lf) {
			def searchString = options.lf
			if (!searchString?.trim()) {
				println "Empty Searchstring for Option"
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
		if (options.log) {
			def patchFile = new File(options.log)
			if (!patchFile.exists() | !patchFile.file) {
				println "Patch File ${options.log} not valid: either not a file or it doesn't exist"
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
		patchClient.cleanLocalMavenRepo()
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

	def savePatch(def patchClient,def options) {
		ObjectMapper mapper = new ObjectMapper()
		def patchData = mapper.readValue(new File(options.sa), Patch.class)
		patchClient.save(patchData)
		def cmdResult = new Expando()
		cmdResult.patchFile = options.sa
		return cmdResult
	}


	def onClone(def patchClient,def options) {
		println "Performing onClone for source=${options.ocs[0]} and target=${options.ocs[1]}"
		def source = options.ocs[0]
		def target = options.ocs[1]
		patchClient.onClone(source,target)
	}


}