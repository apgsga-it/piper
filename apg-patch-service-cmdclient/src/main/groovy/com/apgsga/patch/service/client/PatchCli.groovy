package com.apgsga.patch.service.client


import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.runtime.StackTraceUtils
import org.springframework.core.io.ClassPathResource

import com.apgsga.microservice.patch.api.DbModules
import com.apgsga.microservice.patch.api.Patch
import com.apgsga.microservice.patch.api.ServicesMetaData
import com.fasterxml.jackson.databind.ObjectMapper

import groovy.json.JsonSlurper

class PatchCli {
	
	public static PatchCli create() {
		def patchCli = new PatchCli()
		return patchCli
	}

	private PatchCli() {
		super();
	}
	
	def validComponents = ["db", "aps", "mockdb", "nil"]
	def targetSystemMappings
	def validToStates
	def defaultJdbcConfig
	def validate = true
	def config

	def process(def args) {
		println "apscli running with ${profile} profile"
		println args
		config = parseConfig()
		def cmdResults = new Expando();
		cmdResults.returnCode = 1
		cmdResults.results = [:]
		def options = validateOpts(args)
		if (!options) {
			cmdResults.returnCode = 0
			return cmdResults
		}
		try {
			if (options.l) {
				def result = uploadPatchFiles(options)
				cmdResults.results['l'] = result
			}
			if (options.d) {
				def result = downloadPatchFiles(options)
				cmdResults.results['d'] =  result
			}
			if (options.e) {
				def result = patchExists(options)
				cmdResults.results['e'] =  result
			}
			if (options.fs) {
				def result = findById(options)
				cmdResults.results['f'] = result
			}
			if (options.a) {
				def result = findAndPrintAllPatchIds()
				cmdResults.results['a'] =  result
			}
			if (options.r) {
				def result = removePatch(options)
				cmdResults.results['r'] =  result
			}
			if (options.s) {
				def result = uploadPatch(options)
				cmdResults.results['s']=  result
			}
			if (options.sa) {
				def result = savePatch(options)
				cmdResults.results['sa']=  result
			}
			if (options.dd) {
				def result = downloadDbModules(options)
				cmdResults.results['dd'] = result
			}
			if (options.ud) {
				def result = uploadDbModules(options)
				cmdResults.results['ud'] = result
			}
			if (options.dm) {
				def result = downloadServiceMetaData(options)
				cmdResults.results['dm'] = result
			}
			if (options.um) {
				def result = uploadServiceMetaData(options)
				cmdResults.results['um'] = result
			}
			if (options.sta) {
				def result = stateChangeAction(options)
				cmdResults.results['sta'] = result
			}
			if (options.la) {
				def result = listAllFiles(options)
				cmdResults.results['la'] = result
			}
			if (options.lf) {
				def result = listFiles(options)
				cmdResults.results['lf'] = result
			}
			if (options.oc) {
				def result = onClone(options)
				cmdResults.results['oc'] = result
			}
			if (options.sr) {
				def result = saveRevisions(options)
				cmdResults.results['sr'] = result
			}
			if (options.rr) {
				def result = retrieveRevisions(options)
				cmdResults.results['rr'] = result
			}
			// TODO JHE (26.06.2018): will be removed with JAVA8MIG-389
			if (options.rtr) {
				def result = removeAllTRevisions(options)
				cmdResults.results['rtr'] = result
			}
			if (options.pr) {
				def result = retrieveLastProdRevision()
				cmdResults.results['pr'] = result
			}
			if (options.resr) {
				def result = resetRevision(options)
				cmdResults.results['resr'] = result
			}
			if (options.cr) {
				def result = cleanReleases(options)
				cmdResults.results['cr'] = result
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
	
	private def parseConfig() {
		ClassPathResource res = new ClassPathResource('apscli.properties')
		assert res.exists() : "apscli.properties doesn't exist or is not accessible!"
		ConfigObject conf = new ConfigSlurper(profile).parse(res.URL);
		return conf
	}
	
	private getProfile() {
		def apsCliEnv = System.getProperty("apscli.env")
		// If apscli.env is not define, we assume we're testing
		def prof =  apsCliEnv ?: "test"
		return prof
	}
	
	def validateOpts(args) {
		// TODO JHE: Add oc, sr, rr and rtr description here.
		def cli = new CliBuilder(usage: 'apspli.sh [-u <url>] [-h] [-[l|d|dd|dm] <directory>]  [-[e|r] <patchnumber>] [-[s|sa|ud|um] <file>] [-f <patchnumber,directory>] [-sta <patchnumber,toState,[aps,db,nil]]')
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
			dd longOpt: 'downloadDbmodules', args:1, argName: 'directory', 'Download Dbmodules from server to <directory>', required: false
			ud longOpt: 'uploadDbmodules', args:1, argName: 'file', 'Upload Dbmodules from <file> to server', required: false
			dm longOpt: 'downloadServicesMeta', args:1, argName: 'directory', 'Download ServiceMetaData from server to <directory>', required: false
			um longOpt: 'uploadServicesMeta', args:1, argName: 'file', 'Upload ServiceMetaData from <file> to server', required: false
			la longOpt: 'listAllFiles', 'List all files on server', required: false
			lf longOpt: "listFiles", args:1, argName: 'prefix', 'List all files on server with prefix', required: false
			sta longOpt: 'stateChange', args:3, valueSeparator: ",", argName: 'patchNumber,toState,component', 'Notfiy State Change for a Patch with <patchNumber> to <toState> to a <component> , where <component> can be service,db or null ', required: false
			vv longOpt: 'validateArtifactNamesForVersion', args:2, valueSeparator: ",", argName: 'version,cvsBranch', 'Validate all artifact names for a given version on a given CVS branch', required: false
			oc longOpt: 'onclone', args:1, argName: 'target', 'Call Patch Service onClone REST API', required: false
			sr longOpt: 'saveRevision', args:3, valueSeparator: ",", argName: 'targetInd,installationTarget,revision', 'Save revision file with new value for a given target', required: false
			rr longOpt: 'retrieveRevision', args:2, valueSeparator: ",", argName: 'targetInd,installationTarget', 'Update revision with new value for given target', required: false
			// TODO JHE (26.06.2018): will be removed with JAVA8MIG-389
			rtr longOpt: 'removeTRevisions', args:1, argName: 'dryRun', 'Remove all T Revision from Artifactory. dryRun=1 -> simulation only, dryRun=0 -> artifact will be deleted', required: false
			pr longOpt: 'prodRevision', args:0, 'Retrieve last revision for the production target', required: false
			resr longOpt: 'resetRevision', args:1, argName: 'target', 'Reset revision number for a given target', required: false
			cr longOpt: 'cleanReleases', args:1, argName: 'target', 'Clean release Artifacts for a given target on Artifactory', required: false
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
		
		valdidateAndLoadConfigFiles()

		if (!options | options.h| options.getOptions().size() == 0) {
			cli.usage()
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
		if (options.db && !options.sta) {
			println "No need to have a db configuration, if not using sta"
		}
		if (options.sta) {
			if (options.stas.size() != 3 ) {
				println "Option sta needs 3 arguments: <patchNumber,toState,[db,aps,nil]>"
				error = true
			}
			def patchNumber = options.stas[0]
			if (!patchNumber.isInteger()) {
				println "Patchnumber ${patchNumber} is not a Integer"
				error = true
			}
			def toState = options.stas[1]
			if (!validToStates.contains(toState) ) {
				println "ToState ${toState} not valid: needs to be one of ${validToStates}"
				error = true
			}
			def component = options.stas[2]
			if (component != null && !validComponents.contains(component.toLowerCase()) ) {
				println "Component ${component} not valid: needs to be one of ${validComponents}"
				error = true
			}
			if (options.db) {
				def dbConfigFile = new File(options.db)
				if (!dbConfigFile.exists() || !dbConfigFile.isFile()) {
					println "Db Configfile ${dbConfigFile} not valid: does'nt exist or isn't a file"
					error = true
				}
			}

		}
		if (options.e) {
			if (!options.e.isInteger()) {
				println "Patchnumber ${options.e} is not a Integer"
				error = true
			}
		}
		if (options.vv) {
			if(options.vvs.size() != 2 || options.vvs[0] == null || options.vvs[0].equals("") || options.vvs[1] == null || options.vvs[1].equals("")) {
				println "You have to provide the version and the cvs branch for which you want to validate Artifacts against."
				error = true
			}
		}
		if (options.oc) {
			if(options.ocs.size() != 1 || options.ocs[0] == null) {
				println "You have to provide the target for which the onClone method will be done."
				error = true
			}
		}
		// TODO JHE (26.06.2018): will be removed with JAVA8MIG-389
		if (options.rtr) {
			if(options.rtr.size() != 1) {
				println "No parameter has been set, only a dryRun will be done. To delete all T artifact, please explicitely set dryRun to 0."
				error = true
			}
		}
		if (options.rr) {
			if(options.rrs.size() != 2) {
				println "2 parameters are required for the retrieveRevision command."
				error = true
			}
		}
		if (options.sr) {
			if(options.srs.size() != 3) {
				println "3 parameters are required for the saveRevision command."
				error = true
			}
		}
		if (options.resr) {
			if(options.resrs.size() != 1) {
				println "target parameter is required when reseting revision."
				error = true
			}
		}
		if (options.cr) {
			if(options.crs.size() != 1) {
				println "target parameter is required when cleaning Artifactory releases."
				error = true
			}
		}
		if (error) {
			cli.usage()
			return null
		}
		options
	}
	
	def valdidateAndLoadConfigFiles() {
		// TODO (che, 1.5 ) Make File name Configurable
		def mappingFileName = config.target.system.mapping.file.name
		def configDir = config.config.dir
		def targetSystemMappingsFilePath = "${configDir}/${mappingFileName}"
		def targetSystemFile = new File(targetSystemMappingsFilePath)
		def jsonSystemTargets = new JsonSlurper().parseText(targetSystemFile.text)
		targetSystemMappings = [:]
		jsonSystemTargets.targetSystems.find( { a ->  a.stages.find( { targetSystemMappings.put("${a.name}${it.toState}".toString(),"${it.code}") })} )
		//println "Running with TargetSystemMappings: "
		//println JsonOutput.prettyPrint(targetSystemFile.text)
		// TODO validate
		validToStates = targetSystemMappings.keySet()
		def jdbcConfigFule = new File(configDir, "jdbc.groovy")
		defaultJdbcConfig = new ConfigSlurper().parse(jdbcConfigFule.toURI().toURL())
		// TODO validate
	}

	def stateChangeAction(def options) {
		def patchClient = new PatchServiceClient(config)
		def cmdResult = new Expando()
		def patchNumber = options.stas[0]
		def toState = options.stas[1]
		def component = options.stas[2].toLowerCase()
		cmdResult.patchNumber = patchNumber
		cmdResult.toState = toState
		cmdResult.component = component
		if (component.equals("aps")) {
			patchClient.executeStateTransitionAction(patchNumber,toState)
		} else if (component.equals("db") || component.equals("mockdb")) {
			def dbcli = new PatchDbClient(component,targetSystemMappings)
			dbcli.executeStateTransitionAction(defaultJdbcConfig, patchNumber, toState)
		} else {
			println "Skipping State change Processing for ${patchNumber}"
		}
		return cmdResult
	}

	def findById(def options) {
		def cmdResult = new Expando()
		def patchNumber = options.fs[0]
		def dirName = options.fs[1]
		def found = retrieveAndWritePatch(patchNumber, dirName)
		cmdResult.patchNumber = patchNumber
		cmdResult.dirName = dirName
		cmdResult.exists = found
		return cmdResult
	}

	def downloadPatchFiles(def options) {
		def cmdResult = new Expando()
		List<String> ids =  patchClient.findAllPatchIds()
		ids.each { id ->
			retrieveAndWritePatch(id,options.d)
		}
		cmdResult.patchNumbers = ids
		cmdResult.directory = options.d
		return cmdResult
	}

	def findAndPrintAllPatchIds() {
		def patchClient = new PatchServiceClient(config)
		List<String> ids =  patchClient.findAllPatchIds()
		println "All Patch Ids: ${ids}"
		def cmdResult = new Expando()
		cmdResult.patchNumbers = ids
	}

	def listAllFiles(def options) {
		def patchClient = new PatchServiceClient(config)
		List<String> files =  patchClient.listAllFiles()
		println "All Files on server: ${files}"
		def cmdResult = new Expando()
		cmdResult.files = files
	}

	def listFiles(def options) {
		def patchClient = new PatchServiceClient(config)
		List<String> files =  patchClient.listFiles(options.lf)
		println "Files with ${options.lf} as prefix on server: ${files}"
		def cmdResult = new Expando()
		cmdResult.files = files
	}

	def patchExists(def options) {
		def patchClient = new PatchServiceClient(config)
		def exists = patchClient.patchExists(options.e)
		println "Patch ${options.e} exists is: ${exists} "
		def cmdResult = new Expando()
		cmdResult.exists = exists
		return cmdResult
	}

	def savePatch(def options) {
		def patchClient = new PatchServiceClient(config)
		patchClient.save(new File(options.sa), Patch.class)
		def cmdResult = new Expando()
		cmdResult.patchFile = options.sa
		return cmdResult
	}


	def uploadPatch(def options) {
		def patchClient = new PatchServiceClient(config)
		patchClient.savePatch(new File(options.s), Patch.class)
		def cmdResult = new Expando()
		cmdResult.patchFile = options.s
		return cmdResult
	}


	def removePatch(def options) {
		def patchClient = new PatchServiceClient(config)
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

	def retrieveAndWritePatch(def id, def file) {
		def patchClient = new PatchServiceClient(config)
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

	def uploadPatchFiles(def options) {
		def patchClient = new PatchServiceClient(config)
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

	def downloadDbModules(def options) {
		def patchClient = new PatchServiceClient(config)
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

	def uploadDbModules(def options) {
		def patchClient = new PatchServiceClient(config)
		println "Uploading Dbmodules from ${options.ud}"
		ObjectMapper mapper = new ObjectMapper();
		def dbModules = mapper.readValue(new File("${options.ud}"), DbModules.class)
		patchClient.saveDbModules(dbModules)
	}

	def downloadServiceMetaData(def options) {
		def patchClient = new PatchServiceClient(config)
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

	def uploadServiceMetaData(def options) {
		def patchClient = new PatchServiceClient(config)
		println "Uploading ServiceMetaData from ${options.um}"
		ObjectMapper mapper = new ObjectMapper();
		def serviceMetaData = mapper.readValue(new File("${options.um}"), ServicesMetaData.class)
		patchClient.saveServicesMetaData(serviceMetaData)
	}


	def validateArtifactNamesForVersion(def options) {
		def patchClient = new PatchServiceClient(config)
		println("Validating all Artifact names for version ${options.vvs[0]} on branch ${options.vvs[1]}")
		def invalidArtifacts = patchClient.invalidArtifactNames(options.vvs[0],options.vvs[1])
		println invalidArtifacts
	}

	def onClone(def options) {
//		def patchCloneClient = new PatchCloneClient(config)
		def patchClient = new PatchServiceClient(config)
		println "Performing onClone for ${options.ocs[0]}"
		def target = options.ocs[0]
		patchClient.onClone(target)
	}

	def retrieveRevisions(def options) {
		def patchRevisionClient = new PatchRevisionClient(config)
		patchRevisionClient.retrieveRevisions(options.rrs[0],options.rrs[1])
	}
	
	def retrieveLastProdRevision() {
		def patchRevisionClient = new PatchRevisionClient(config)
		patchRevisionClient.retrieveLastProdRevision()
	}

	def saveRevisions(def options) {
		def patchRevisionClient = new PatchRevisionClient(config)
		patchRevisionClient.saveRevisions(options.srs[0],options.srs[1],options.srs[2])
	}

	// TODO JHE (26.06.2018): will be removed with JAVA8MIG-389
	def removeAllTRevisions(def options) {
		def patchArtifactoryClient = new PatchArtifactoryClient(config)
		def dryRun = true
		if(options.rtrs[0] == 0) {
			dryRun = false
		}
		patchArtifactoryClient.deleteAllTRevisions(dryRun)
	}
	
	def resetRevision(def options) {
		def patchRevisionClient = new PatchRevisionClient(config)
		def target = options.resrs[0]
		patchRevisionClient.resetLastRevision(target)
	}
	
	def cleanReleases(def options) {
		def target = options.crs[0]
		def patchArtifactoryClient = new PatchArtifactoryClient(config)
		patchArtifactoryClient.cleanReleases(target)
	}
}