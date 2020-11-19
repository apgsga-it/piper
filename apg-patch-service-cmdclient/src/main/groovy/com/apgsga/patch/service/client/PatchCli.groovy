package com.apgsga.patch.service.client

import com.apgsga.microservice.patch.api.Patch
import com.apgsga.microservice.patch.api.PatchLogDetails
import com.apgsga.patch.db.integration.impl.NotifyDbParameters
import com.apgsga.patch.service.client.rest.PatchRestServiceClient
import com.google.common.collect.Maps
import org.codehaus.groovy.runtime.StackTraceUtils

class PatchCli {

	static PatchCli create() {
		def patchCli = new PatchCli()
		return patchCli
	}

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
			} else if (options.build) {
				def result = doBuild(patchClient,options)
				cmdResults.results['build'] = result
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
			} else if (options.cpf) {
				def status = options.cpfs[0]
				def destFolder = options.cpfs[1]
				cmdResults.result = copyPatchFile(patchClient,status,destFolder)
			} else if (options.setup) {
				cmdResults.result = doSetup(patchClient,options)
			} else if (options.notifydb) {
				cmdResults.result = doNotifyDb(patchClient,options)
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
		def cli = new CliBuilder(usage: 'apspli.sh [-u <url>] [-h] [-purl <piperUrl>] [-i <target>] [-cpf <statusCode,destFolder>] [-dbsta <patchNumber,toState>] [-adp <target>] [-log <patchNumber>] [-cm] [-sa <patchFile>] [-sta <patchnumber,toState,[aps]]  [-sjsp <jobName,jobParams>]')
		cli.formatter.setDescPadding(0)
		cli.formatter.setLeftPadding(0)
		cli.formatter.setWidth(100)
		cli.with {
			h longOpt: 'help', 'Show usage information', required: false
			purl longOpt: 'piperUrl', args:1, argName: 'piperUrl', 'Piper URL', required: false
			sa longOpt: 'save', args:1, argName: 'patchFile', 'Saves a <patchFile> to the server, which starts the Patch Pipeline', required: false
			build longOpt: 'build', args:3, valueSeparator: ",", argName: 'patchNumber,stage,successNotification', 'Build a patch for a stage. eg.: 8000,Informatiktest,doneOk', required: false
			cm longOpt: 'cleanLocalMavenRepo', "Clean local Maven Repo used bei service", required: false
			log longOpt: 'log', args:4, valueSeparator: ",", argName: 'patchNumber,target,step,text', 'Log a patch steps for a patch', required: false
			adp longOpt: 'assembleDeployPipeline', args:1, argName: 'target', "starts an assembleAndDeploy pipeline for the given target", required: false
			cpf longOpt: 'copyPatchFiles', args:2, valueSeparator: ",", argName: "statusCode,destFolder", 'Copy patch files for a given status into the destfolder', required: false
			i longOpt: 'install', args:1, argName: 'target', "starts an install pipeline for the given target", required: false
			setup longOpt: 'setup', args:2, valueSeparator: ",", argName: 'patchNumber,successNotification', 'Starts setup for a patch, required before beeing ready to build', required: false
			notifydb longOpt: 'notifdb', args:3, valueSeparator: ",", argName: "patchNumber,stage,successNotification", 'Notify the DB that a Job has been done successfully', required: false
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

		if (options.build) {
			if (options.builds.size() != 3 ) {
				println "Option build needs 3 arguments: <patchNumber,stage,successNotification>"
				error = true
			}
			def patchNumber = options.builds[0]
			if (!patchNumber.isInteger()) {
				println "Patchnumber ${patchNumber} is not a Integer"
				error = true
			}
		}

		if (options.setup) {
			if (options.setups.size() != 2 ) {
				println "Option build needs 2 arguments: <patchNumber,successNotification>"
				error = true
			}
			def patchNumber = options.setups[0]
			if (!patchNumber.isInteger()) {
				println "Patchnumber ${patchNumber} is not a Integer"
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
			if (options.logs.size() != 4) {
				println "Logging activity requires a Patch number, target, step and text"
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
		if (options.notifydb) {
			if (options.notifydbs.size() != 3 ) {
				println "Option sta needs 3 arguments: <patchNumber,stage,successnotification>"
				error = true
			}
			def patchNumber = options.notifydbs[0]
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

	static def doBuild(def patchClient, def options) {
		def cmdResult = new Expando()
		def patchNumber = options.builds[0]
		def stage = options.builds[1]
		def successNotification = options.builds[2]
		cmdResult.patchNumber = patchNumber
		cmdResult.stage = stage
		cmdResult.successNotification = successNotification
		patchClient.build(patchNumber,stage,successNotification)
		return cmdResult
	}

	static def doSetup(def patchClient, def options) {
		def cmdResult = new Expando()
		def patchNumber = options.setups[0]
		def successNotification = options.setups[1]
		cmdResult.patchNumber = patchNumber
		cmdResult.successNotification = successNotification
		patchClient.setup(patchNumber,successNotification)
		return cmdResult
	}

	static def doNotifyDb(def patchClient, def options) {
		def cmdResult = new Expando()
		def patchNumber = options.notifydbs[0]
		def stage = options.notifydbs[1]
		def successNotification = options.notifydbs[2]
		cmdResult.patchNumber = patchNumber
		cmdResult.stage = stage
		cmdResult.successNotification = successNotification
		NotifyDbParameters params = NotifyDbParameters.create(patchNumber).stage(stage).successNotification(successNotification)
		patchClient.notifyDb(params)
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
		def target = options.logs[1]
		def step = options.logs[2]
		def text = options.logs[3]
		PatchLogDetails pld = new PatchLogDetails()
		pld.setTarget(target)
		pld.setPatchPipelineTask(step)
		pld.setLogText(text)
		pld.setDateTime(new Date())
		println "Logging patch activity for patch number ${patchNumber} with following info : target=${target},step=${step},text=${text}"
		patchClient.savePatchLog(patchNumber,pld)
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

	def copyPatchFile(PatchRestServiceClient patchClient, def status, def destFolder) throws Exception {
		Map params = Maps.newHashMap()
		params.put("status",status)
		params.put("destFolder",destFolder)
		patchClient.copyPatchFiles(params)
	}


	private def fetchPiperUrl(def options) {
		if(options.purls && options.purls.size() == 1) {
			return options.purls[0]
		}
		else {
			return System.getProperty("piper.host.default.url")
		}
	}

}