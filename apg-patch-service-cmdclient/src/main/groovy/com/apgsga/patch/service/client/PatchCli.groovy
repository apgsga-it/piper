package com.apgsga.patch.service.client


import org.codehaus.groovy.runtime.StackTraceUtils
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.core.io.ClassPathResource

import com.apgsga.microservice.patch.api.DbModules
import com.apgsga.microservice.patch.api.Patch
import com.apgsga.microservice.patch.api.PatchLog
import com.apgsga.microservice.patch.api.PatchLogDetails
import com.apgsga.microservice.patch.api.ServicesMetaData
import com.apgsga.microservice.patch.api.impl.PatchLogBean
import com.apgsga.microservice.patch.api.impl.PatchLogDetailsBean
import com.apgsga.patch.service.client.utils.TargetSystemMappings
import com.fasterxml.jackson.databind.ObjectMapper
import com.apgsga.patch.service.client.utils.AppContext

import groovy.json.JsonSlurper

class PatchCli {

	public static PatchCli create() {
		def patchCli = new PatchCli()
		return patchCli
	}

	private PatchCli() {
		super();
	}

	def validComponents = [ "aps", "nil"]
	def validate = true
	def config

	def process(def args) {
		config = AppContext.instance.load()
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
			def patchClient = new PatchServiceClient(config)
			if (options.l) {
				def result = uploadPatchFiles(patchClient,options)
				cmdResults.results['l'] = result
			}
			if (options.d) {
				def result = downloadPatchFiles(patchClient,options)
				cmdResults.results['d'] =  result
			}
			if (options.e) {
				def result = patchExists(patchClient,options)
				cmdResults.results['e'] =  result
			}
			if (options.fs) {
				def result = findById(patchClient,options)
				cmdResults.results['f'] = result
			}
			if (options.a) {
				def result = findAndPrintAllPatchIds(patchClient,options)
				cmdResults.results['a'] =  result
			}
			if (options.r) {
				def result = removePatch(patchClient,options)
				cmdResults.results['r'] =  result
			}
			if (options.s) {
				def result = uploadPatch(patchClient,options)
				cmdResults.results['s']=  result
			}
			if (options.sa) {
				def result = savePatch(patchClient,options)
				cmdResults.results['sa']=  result
			}
			if (options.redo) {
				def result = redoPatch(patchClient,options)
				cmdResults.results['sa']=  result
			}
			if (options.dd) {
				def result = downloadDbModules(patchClient,options)
				cmdResults.results['dd'] = result
			}
			if (options.ud) {
				def result = uploadDbModules(patchClient,options)
				cmdResults.results['ud'] = result
			}
			if (options.dm) {
				def result = downloadServiceMetaData(patchClient,options)
				cmdResults.results['dm'] = result
			}
			if (options.um) {
				def result = uploadServiceMetaData(patchClient,options)
				cmdResults.results['um'] = result
			}
			if (options.sta) {
				def result = stateChangeAction(patchClient,options)
				cmdResults.results['sta'] = result
			}
			if (options.la) {
				def result = listAllFiles(patchClient,options)
				cmdResults.results['la'] = result
			}
			if (options.lf) {
				def result = listFiles(patchClient,options)
				cmdResults.results['lf'] = result
			}
			if (options.oc) {
				def result = onClone(patchClient,options)
				cmdResults.results['oc'] = result
			}
			if (options.cr) {
				def result = cleanReleases(options)
				cmdResults.results['cr'] = result
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
		// TODO JHE: Add oc, sr, rr and rtr description here.
		def cli = new CliBuilder(usage: 'apspli.sh [-u <url>] [-h] [-[l|d|dd|dm] <directory>]  [-[e|r] <patchnumber>] [-[s|sa|ud|um] <file>] [-f <patchnumber,directory>] [-sta <patchnumber,toState,[aps]]')
		cli.formatter.setDescPadding(0)
		cli.formatter.setLeftPadding(0)
		cli.formatter.setWidth(100)
		cli.with {
			h longOpt: 'help', 'Show usage information', required: false
			l longOpt: 'upload', args:1 , argName: 'directory', 'Upload all Patch files <directory> ', required: false
			d longOpt: 'download', args:1 , argName: 'directory', 'Download all Patch files to a <directory>', required: false
			e longOpt: "exists", args:1, argName: 'patchNumber', 'True resp. false if Patch of the <patchNumber> exists or not', required: false
			f longOpt: 'findById', args:2, valueSeparator: ",", argName: 'patchNumber,directory','Retrieve a Patch with the <patchNumber> to a <directory>', required: false
			a longOpt: 'findAllIds','Retrieve and print all PatchIds', required: false
			r longOpt: 'remove', args:1, argName: 'patchNumber', 'Remove Patch with <patchNumber>', required: false
			s longOpt: 'save', args:1, argName: 'patchFile', 'Uploads a <patchFile> to the server', required: false
			sa longOpt: 'save', args:1, argName: 'patchFile', 'Saves a <patchFile> to the server, which starts the Patch Pipeline', required: false
			redo longOpt: 'redo', args:1, argName: 'patchNumber', 'Restarts a patch Patch Pipeline', required: false
			dd longOpt: 'downloadDbmodules', args:1, argName: 'directory', 'Download Dbmodules from server to <directory>', required: false
			ud longOpt: 'uploadDbmodules', args:1, argName: 'file', 'Upload Dbmodules from <file> to server', required: false
			dm longOpt: 'downloadServicesMeta', args:1, argName: 'directory', 'Download ServiceMetaData from server to <directory>', required: false
			um longOpt: 'uploadServicesMeta', args:1, argName: 'file', 'Upload ServiceMetaData from <file> to server', required: false
			la longOpt: 'listAllFiles', 'List all files on server', required: false
			lf longOpt: "listFiles", args:1, argName: 'prefix', 'List all files on server with prefix', required: false
			// TODO (CHE,13.9) Get rid of the component parameter, needs to be coordinated with current Patch System (PatchOMat)
			sta longOpt: 'stateChange', args:3, valueSeparator: ",", argName: 'patchNumber,toState,component', 'Notfiy State Change for a Patch with <patchNumber> to <toState> to a <component> , where <component> can only be aps ', required: false
			oc longOpt: 'onclone', args:2, valueSeparator: ",", argName: 'source,target', 'Call Patch Service onClone REST API', required: false
			cm longOpt: 'cleanLocalMavenRepo', "Clean local Maven Repo used bei service", required: false
			// TODO (JHE, CHE, 12.9 ) move this to own cli
			cr longOpt: 'cleanReleases', args:1, argName: 'target', 'Clean release Artifacts for a given target on Artifactory', required: false
			log longOpt: 'log', args:1, argName: 'patchFile', 'Log a patch steps for a patch', required: false
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
		if (options.l) {
			def directory = new File(options.l)
			if (!directory.exists() | !directory.directory) {
				println "Directory ${options.l} not valid: either not a directory or it doesn't exist"
				error = true
			}
		}
		if (options.lf) {
			def searchString = options.lf
			if (!searchString?.trim()) {
				println "Empty Searchstring for Option"
				error = true;
			}
		}
		if (options.d) {
			def directory = new File(options.d)
			if (!directory.exists() | !directory.directory) {
				println "Directory ${options.d} not valid: either not a directory or it doesn't exist"
				error = true
			}
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
		if (options.r) {
			if (!options.r.isInteger()) {
				println "Patchnumber ${options.r} is not a Integer"
				error = true
			}
		}
		if (options.s) {
			def patchFile = new File(options.s)
			if (!patchFile.exists() | !patchFile.file) {
				println "Patch File ${options.s} not valid: either not a file or it doesn't exist"
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

		if (options.dd) {
			def directory = new File(options.dd)
			if (!directory.exists() | !directory.directory) {
				println "Directory ${options.dd} not valid: either not a directory or it doesn't exist"
				error = true
			}
		}
		if (options.ud) {
			def dataFile = new File(options.ud)
			if (!dataFile.exists() | !dataFile.file) {
				println "File ${options.ud} not valid: either not a file or it doesn't exist"
				error = true
			}
		}
		if (options.dm) {
			def directory = new File(options.dm)
			if (!directory.exists() | !directory.directory) {
				println "Directory ${options.dm} not valid: either not a directory or it doesn't exist"
				error = true
			}
		}
		if (options.um) {
			def dataFile = new File(options.um)
			if (!dataFile.exists() | !dataFile.file) {
				println "File ${options.um} not valid: either not a file or it doesn't exist"
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
		if (options.redo) {
			if (!options.redo.isInteger()) {
				println "Patchnumber ${options.redo} is not a Integer"
				error = true
			}
		}
		if (options.oc) {
			if(options.ocs.size() != 2) {
				println "You have to provide the source and target for which the onClone method will be done."
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

	def downloadPatchFiles(def patchClient,def options) {
		def cmdResult = new Expando()
		List<String> ids =  patchClient.findAllPatchIds()
		ids.each { id ->
			retrieveAndWritePatch(id,options.d)
		}
		cmdResult.patchNumbers = ids
		cmdResult.directory = options.d
		return cmdResult
	}

	def findAndPrintAllPatchIds(def patchClient,def options) {
		List<String> ids =  patchClient.findAllPatchIds()
		println "All Patch Ids: ${ids}"
		def cmdResult = new Expando()
		cmdResult.patchNumbers = ids
	}

	def listAllFiles(def patchClient,def options) {
		List<String> files =  patchClient.listAllFiles()
		println "All Files on server: ${files}"
		def cmdResult = new Expando()
		cmdResult.files = files
	}

	def listFiles(def patchClient,def options) {
		List<String> files =  patchClient.listFiles(options.lf)
		println "Files with ${options.lf} as prefix on server: ${files}"
		def cmdResult = new Expando()
		cmdResult.files = files
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
	
	def redoPatch(def patchClient,def options) {
		patchClient.restartProdPipeline(options.redo)
		def cmdResult = new Expando()
		cmdResult.patchFile = options.redo
		return cmdResult
	}


	def uploadPatch(def patchClient,def options) {
		patchClient.savePatch(new File(options.s), Patch.class)
		def cmdResult = new Expando()
		cmdResult.patchFile = options.s
		return cmdResult
	}


	def removePatch(def patchClient,def options) {
		println "Reading: ${options.r} to remove from server"
		Patch patchData = patchClient.findById(options.r)
		println "Removing Patch ${options.r}"
		assert patchData != null : "Patch ${options.r} to remove not found"
		patchClient.removePatch(patchData)
		println "Remove Patch ${options.r} done."
		def cmdResult = new Expando();
		cmdResult.patchNumber = options.r
		cmdResult.patchData = patchData.toString()
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

	def uploadPatchFiles(def patchClient,def options) {
		def found = false
		def cmdResult = new Expando()
		cmdResult.fileNames = []
		ObjectMapper mapper = new ObjectMapper();
		new File(options.l).eachFileMatch(~"^Patch.*.json") { file ->
			patchClient.savePatch(file, Patch.class)
			cmdResult.fileNames << file.absolutePath
			found = true
		}
		if (!found) {
			println "No patch files found in ${options.l}"
		}
		cmdResult.directory = options.l
		cmdResult.found = found
		return cmdResult
	}

	def downloadDbModules(def patchClient,def options) {
		println "Downloading Dbmodules to ${options.dd}"
		def cmdResult = new Expando()
		def dbmodules =  patchClient.getDbModules()
		if (dbmodules == null) {
			cmdResult.exists = false;
			return cmdResult
		}
		def dbModulesFile = new File(options.dd,"DbModules.json")
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(dbModulesFile, dbmodules)
		println "Downloaded Dbmodules to ${options.dd} done."
		cmdResult.dbModulesFile = dbModulesFile.absolutePath
		cmdResult.data = dbmodules
		cmdResult.exists = true;
		return cmdResult
	}

	def uploadDbModules(def patchClient,def options) {
		println "Uploading Dbmodules from ${options.ud}"
		ObjectMapper mapper = new ObjectMapper();
		def dbModules = mapper.readValue(new File("${options.ud}"), DbModules.class)
		patchClient.saveDbModules(dbModules)
	}

	def downloadServiceMetaData(def patchClient,def options){
		println "Downloading ServiceMetaData to ${options.dm}"
		def cmdResult = new Expando()
		def data =  patchClient.getServicesMetaData()
		if (data == null) {
			cmdResult.exists = false;
			return cmdResult
		}
		def dataFile = new File(options.dm,"ServicesMetaData.json")
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(dataFile, data)
		println "Downloaded ServiceMetaData to ${options.dm} done."
		cmdResult.serviceMetaDataFile = dataFile.absolutePath
		cmdResult.data = data
		cmdResult.exists = true;
		return cmdResult
	}

	def uploadServiceMetaData(def patchClient,def options) {
		println "Uploading ServiceMetaData from ${options.um}"
		ObjectMapper mapper = new ObjectMapper();
		def serviceMetaData = mapper.readValue(new File("${options.um}"), ServicesMetaData.class)
		patchClient.saveServicesMetaData(serviceMetaData)
	}


	def onClone(def patchClient,def options) {
		println "Performing onClone for source=${options.ocs[0]} and target=${options.ocs[1]}"
		def source = options.ocs[0]
		def target = options.ocs[1]
		patchClient.onClone(source,target)
	}

	def cleanReleases(def options) {
		def target = options.crs[0].toUpperCase()
		def patchArtifactoryClient = new PatchArtifactoryClient(config)
		patchArtifactoryClient.cleanReleases(target)
	}
	
	def logPatchActivity(def patchClient,def options) {
		println "Logging patch activity for ${options.logs[0]}"
		ObjectMapper mapper = new ObjectMapper();
		def patchFile = mapper.readValue(new File("${options.logs[0]}"), Patch.class)
		patchClient.savePatchLog(patchFile)		
	}
}