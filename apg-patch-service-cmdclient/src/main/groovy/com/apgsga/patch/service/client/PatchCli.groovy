package com.apgsga.patch.service.client

import com.apgsga.microservice.patch.api.JenkinsParameterType
import com.apgsga.microservice.patch.api.Patch
import com.apgsga.patch.service.client.rest.PatchRestServiceClient
import com.google.common.collect.Maps
import org.codehaus.groovy.runtime.StackTraceUtils

class PatchCli {

	static PatchCli create() {
		def patchCli = new PatchCli()
		return patchCli
	}

	// TODO JHE (19.08.2020) : Still need aps and nil ???
	def validComponents = [ "aps", "nil"]
	def validate = true

	private PatchCli() {
	}

	def process(def args) {
		def cmdResults = new Expando()
		cmdResults.returnCode = 1
		cmdResults.results = [:]
		def options = validateOpts(args)
		if (!options) {
			cmdResults.returnCode = 0
			return cmdResults
		}
		def piperUrl = fetchPiperUrl(options)
		if (!piperUrl?.trim()) {
			cmdResults.returnCode = 0
			cmdResults.results['error'] = "Piper URL not configured"
			return cmdResults
		}
		try {
			def patchClient = new PatchRestServiceClient(piperUrl)
			if (options.sa) {
				def result = savePatch(patchClient,options)
				cmdResults.results['sa']=  result
			} else if (options.sta) {
				def result = stateChangeAction(patchClient,options)
				cmdResults.results['sta'] = result
			} else if (options.cm) {
				def result = cleanLocalMavenRepo(patchClient)
				cmdResults.results['cm'] = result
			} else if (options.log) {
				def result = logPatchActivity(patchClient,options)
				cmdResults.results['log'] = result
			} else if (options.adp) {
				def result = assembleAndDeployPipeline(patchClient, options)
				cmdResults.results['adp'] = result
			} else if (options.i) {
				def result = installPipeline(patchClient, options)
				cmdResults.results['i'] = result
			} else if (options.dbsta) {
				def patchNumber = options.dbstas[0]
				def statusNum = Long.valueOf(options.dbstas[1])
				executeStateTransitionActionInDb(patchClient,patchNumber,statusNum)
			} else if (options.cpf) {
				def status = options.cpfs[0]
				def destFolder = options.cpfs[1]
				cmdResults.result = copyPatchFile(patchClient,status,destFolder)
			} else if (options.sj) {
				def jobName = options.sjs[0]
				cmdResults.result = startJenkinsJob(patchClient,jobName,null,null)
			} else if (options.sjsp) {
				def jobName = options.sjsps[0]
				def params = options.sjsps[1]
				cmdResults.result = startJenkinsJob(patchClient,jobName,params,null)
			} else if (options.sjfp) {
				def jobName = options.sjfps[0]
				def params = options.sjfps[1]
				cmdResults.result = startJenkinsJob(patchClient,jobName,null,params)
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
		def cli = new CliBuilder(usage: 'apspli.sh [-u <url>] [-h] [-purl <piperUrl>] [-i <target>] [-cpf <statusCode,destFolder>] [-dbsta <patchNumber,toState>] [-adp <target>] [-log <patchNumber>] [-cm] [-sa <patchFile>] [-sta <patchnumber,toState,[aps]] [-sj <jobName>] [-sjsp <jobName,jobParams>]')
		cli.formatter.setDescPadding(0)
		cli.formatter.setLeftPadding(0)
		cli.formatter.setWidth(100)
		cli.with {
			h longOpt: 'help', 'Show usage information', required: false
			purl longOpt: 'piperUrl', args:1, argName: 'piperUrl', 'Piper URL', required: false
			sa longOpt: 'save', args:1, argName: 'patchFile', 'Saves a <patchFile> to the server, which starts the Patch Pipeline', required: false
			// TODO (CHE,13.9) Get rid of the component parameter, needs to be coordinated with current Patch System (PatchOMat)
			sta longOpt: 'stateChange', args:3, valueSeparator: ",", argName: 'patchNumber,toState,component', 'Notfiy State Change for a Patch with <patchNumber> to <toState> to a <component> , where <component> can only be aps ', required: false
			cm longOpt: 'cleanLocalMavenRepo', "Clean local Maven Repo used bei service", required: false
			log longOpt: 'log', args:1, argName: 'patchNumber', 'Log a patch steps for a patch', required: false
			adp longOpt: 'assembleDeployPipeline', args:1, argName: 'target', "starts an assembleAndDeploy pipeline for the given target", required: false
			dbsta longOpt: 'dbstateChange', args:2, valueSeparator: ",", argName: 'patchNumber,toState', 'Notfiy State Change for a Patch with <patchNumber> to <toState> to the database', required: false
			cpf longOpt: 'copyPatchFiles', args:2, valueSeparator: ",", argName: "statusCode,destFolder", 'Copy patch files for a given status into the destfolder', required: false
			i longOpt: 'install', args:1, argName: 'target', "starts an install pipeline for the given target", required: false
			sj longOpt: 'startJenkinsJob', args:1, argName: 'jobName', "starts a jenkins job without job parameter", required: false
			sjsp longOpt: 'startJenkinsJobWithStringParam', args:2, valueSeparator: ",", argName: 'jobName,jobParams', "start a jenkins job with a list of jenkins job parameter (p1@=v1@:p2@=v2@:p3@=v3)", required: false
			sjfp longOpt: 'startJenkinsJobWithFileParam', args:2, valueSeparator: ",", argName: 'jobName,jobParams', "start a jenkins job with a list of jenkins job parameter (p1@=v1@:p2@=v2@:p3@=v3). Values are path to file", required: false
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
			return null
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
			def component = options.stas[2]
			if (component != null && !validComponents.contains(component.toLowerCase()) ) {
				println "Component ${component} not valid: needs to be one of ${validComponents}"
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
			if (options.logs.size() != 1) {
				println "Logging activity requires a Patch number as Parameter"
				error = true
			}
		}
		if (options.adp) {
			if(options.adps.size() != 1) {
				println "target parameter is required."
				error = true
			}
		}
		if (options.i) {
			if(options.is.size() != 1) {
				println "target parameter is required."
				error = true
			}
		}
		if (options.dbsta) {
			if (options.dbstas.size() != 2 ) {
				println "Option sta needs 2 arguments: <patchNumber,toState>>"
				error = true
			}
			def patchNumber = options.dbstas[0]
			if (!patchNumber.isInteger()) {
				println "Patchnumber ${patchNumber} is not a Integer"
				error = true
			}
		}

		if(options.cpf) {
			if(options.cpfs.size() != 2) {
				println "status and destFolder are required when copying patch files"
				error = true
			}
		}

		if(options.sj) {
			if(options.sjs.size() != 1) {
				println "Job name has to be provided when starting a job"
				error = true
			}
		}

		if(options.sjsp) {
			if(options.sjsps.size() != 2) {
				println "Job name and parameter(s) have to be provided when starting a job with string params"
				error = true
			}
		}

		if(options.sjfp) {
			if(options.sjfps.size() != 2) {
				println "Job name and parameter(s) have to be provided when starting a job with file params"
				error = true
			}
		}

		if (error) {
			cli.usage()
			return null
		}
		options
	}

	static def cleanLocalMavenRepo(def patchClient) {
		def cmdResult = new Expando()
		patchClient.cleanLocalMavenRepo()
		cmdResult
	}

	static def stateChangeAction(def patchClient, def options) {
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

	static def savePatch(def patchClient, def options) {
		patchClient.save(new File(options.sa), Patch.class)
		def cmdResult = new Expando()
		cmdResult.patchFile = options.sa
		return cmdResult
	}

	static def logPatchActivity(def patchClient, def options) {
		def patchNumber = options.logs[0]
		println "Logging patch activity for patch number ${patchNumber}"
		patchClient.savePatchLog(patchNumber)
	}

	static def assembleAndDeployPipeline(def patchClient, def options) {
		def target = options.adps[0]
		println "Starting assembleAndDeploy pipeline for ${target}"
		patchClient.startAssembleAndDeployPipeline(target)
	}

	static def installPipeline(def patchClient, def options) {
		def target = options.is[0]
		println "Starting install pipeline for ${target}"
		patchClient.startInstallPipeline(target)
	}

	def executeStateTransitionActionInDb(PatchRestServiceClient patchClient, def patchNumber, def statusNum) {
		patchClient.executeStateTransitionActionInDb(patchNumber,statusNum)
	}

	def copyPatchFile(PatchRestServiceClient patchClient, def status, def destFolder) throws Exception {
		Map params = Maps.newHashMap()
		params.put("status",status)
		params.put("destFolder",destFolder)
		patchClient.copyPatchFiles(params)
	}

	void startJenkinsJob(PatchRestServiceClient patchClient, def jobName, def stringParams, def fileParams) {
		if(stringParams == null && fileParams == null) {
			patchClient.startJenkinsJob(jobName)
		}
		else {
			def params = [:]
			params.put(JenkinsParameterType.STRING_PARAM,stringParametersAsMap(stringParams))
			params.put(JenkinsParameterType.FILE_PARAM,fileParameterAsMap(fileParams))
			patchClient.startJenkinsJob(jobName,params)
		}

	}

	private Map<String,String> fileParameterAsMap(def fileParams) {
		if(fileParams == null) {
			return null
		}

		def paramAsMap = [:]
		// stringParams has the following form: p1@=v1@:p2@=v2@:p3@=v3
		def keyPair = fileParams.split("@:")
		keyPair.each {kp ->
			def values = kp.split("@=")
			paramAsMap.put(values[0],values[1])
		}

		return paramAsMap
	}

	private Map<String,String> stringParametersAsMap(def stringParams) {
		if(stringParams == null) {
			return null
		}

		def paramAsMap = [:]
		// stringParams has the following form: p1@=v1@:p2@=v2@:p3@=v3
		def keyPair = stringParams.split("@:")
		keyPair.each {kp ->
			def values = kp.split("@=")
			paramAsMap.put(values[0],values[1])
		}

		return paramAsMap
	}

	private def fetchPiperUrl(def options ) {
		if(options.purls && options.purls.size() == 1) {
			return options.purls[0]
		}
		else {
			return System.getProperty("piper.host.default.url")
		}
	}
}