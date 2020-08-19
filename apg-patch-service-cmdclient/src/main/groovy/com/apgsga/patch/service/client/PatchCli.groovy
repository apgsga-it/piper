package com.apgsga.patch.service.client

import com.apgsga.microservice.patch.api.Patch
import com.apgsga.patch.service.client.config.PliConfig
import com.apgsga.patch.service.client.rest.PatchRestServiceClient
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Maps
import groovy.json.JsonBuilder
import org.codehaus.groovy.runtime.StackTraceUtils
import org.springframework.context.annotation.AnnotationConfigApplicationContext

class PatchCli {

	static PatchCli create() {
		def patchCli = new PatchCli()
		return patchCli
	}

	def validComponents = [ "aps", "nil"]
	def validate = true
	def config
	def dbConnection

	private PatchCli() {
	}

	def process(def args) {
		def context =  new AnnotationConfigApplicationContext(PliConfig.class)
		config = context.getBean(ConfigObject.class)
		def cmdResults = new Expando()
		cmdResults.returnCode = 1
		cmdResults.results = [:]
		def options = validateOpts(args)
		if (!options) {
			cmdResults.returnCode = 0
			return cmdResults
		}
		try {
			def patchClient = new PatchRestServiceClient(config)
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
			} else if(options.lpac) {
				def status = options.lpacs[0]
				def filePath = "${config.postclone.list.patch.filepath.template}${status}.json"
				cmdResults.result = listPatchAfterClone(status,filePath)
			} else if (options.dbsta) {
				def patchNumber = options.dbstas[0]
				def toState = options.dbstas[1]
				executeStateTransitionActionInDb(patchClient,patchNumber,toState)
			} else if (options.cpf) {
				def status = options.cpfs[0]
				def destFolder = options.cpfs[1]
				cmdResults.result = copyPatchFile(patchClient,status,destFolder)
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
			sa longOpt: 'save', args:1, argName: 'patchFile', 'Saves a <patchFile> to the server, which starts the Patch Pipeline', required: false
			// TODO (CHE,13.9) Get rid of the component parameter, needs to be coordinated with current Patch System (PatchOMat)
			sta longOpt: 'stateChange', args:3, valueSeparator: ",", argName: 'patchNumber,toState,component', 'Notfiy State Change for a Patch with <patchNumber> to <toState> to a <component> , where <component> can only be aps ', required: false
			cm longOpt: 'cleanLocalMavenRepo', "Clean local Maven Repo used bei service", required: false
			log longOpt: 'log', args:1, argName: 'patchFile', 'Log a patch steps for a patch', required: false
			adp longOpt: 'assembleDeployPipeline', args:1, argName: 'target', "starts an assembleAndDeploy pipeline for the given target", required: false
			lpac longOpt: 'listPatchAfterClone', args:1, argName: 'status', 'Get list of patches to be re-installed after a clone', required: false
			dbsta longOpt: 'dbstateChange', args:2, valueSeparator: ",", argName: 'patchNumber,toState', 'Notfiy State Change for a Patch with <patchNumber> to <toState> to the database', required: false
			cpf longOpt: 'copyPatchFiles', args:2, valueSeparator: ",", argName: "statusCode,destFolder", 'Copy patch files for a given status into the destfolder', required: false
			i longOpt: 'install', args:1, argName: 'target', "starts an install pipeline for the given target", required: false
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
		if (options.i) {
			if(options.is.size() != 1) {
				println "target parameter is required."
				error = true
			}
		}
		if(options.lpac) {
			if(options.lpacs.size() != 1) {
				println("Target status is required when fetching list of patch to be re-installed.")
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
		println "Logging patch activity for ${options.logs[0]}"
		ObjectMapper mapper = new ObjectMapper()
		def patchFile = mapper.readValue(new File("${options.logs[0]}"), Patch.class)
		patchClient.savePatchLog(patchFile)
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

	def listPatchAfterClone(def status, def filePath) {
		String sql = "SELECT id FROM cm_patch_install_sequence_f WHERE ${status}=1 AND (produktion = 0 OR chronology > trunc(SYSDATE))"
		def patchNumbers = []
		try {
			dbConnection.eachRow(sql) { row ->
				def rowId = row.ID
				patchNumbers.add(rowId)
			}

		}
		catch(Exception ex) {
			// TODO JHE(11.04.2019): because the caller will read the stdout in order to determine if all went well ... we can't write the error message. But we need to find a way to log the exception.
			println ex.getMessage()
			println ex.getStackTrace()
			println false
			return
		}

		// TODO (jhe, che, 19.9) have filePath passed as parameter and not preconfigured
		// TODO Or write it stdout , but without any other println
		def listPatchFile = new File(filePath)

		if(listPatchFile.exists()) {
			listPatchFile.delete()
		}

		listPatchFile.write(new JsonBuilder(patchlist:patchNumbers).toPrettyString())
		println true
	}

	// TODO JHE (18.08.2020): do we still want to return anything now that things will be done server-side?
	def executeStateTransitionActionInDb(PatchRestServiceClient patchClient, def patchNumber, def toStatus) {
		patchClient.executeStateTransitionActionInDb(patchNumber,toStatus)
	}

	def copyPatchFile(PatchRestServiceClient patchClient, def status, def destFolder) throws Exception {
		Map params = Maps.newHashMap()
		params.put("status",status)
		params.put("destFolder",destFolder)
		patchClient.copyPatchFiles(params)
	}
}