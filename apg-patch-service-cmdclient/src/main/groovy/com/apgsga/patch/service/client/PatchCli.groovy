package com.apgsga.patch.service.client


import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.runtime.StackTraceUtils
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader

import com.apgsga.microservice.patch.api.DbModules
import com.apgsga.microservice.patch.api.Patch
import com.apgsga.microservice.patch.api.ServicesMetaData
import com.fasterxml.jackson.databind.ObjectMapper

import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class PatchCli {
	
	protected final static Log LOGGER = LogFactory.getLog(PatchCli.class);

	// TODO JHE (12.06.2018) : can we change this with a setter? Not sure as we bootstrap the profile within create() ... if we assign a profile afterwards, obviously it will be too late.
	public static PatchCli create(String p_profile) {
		profile = p_profile
		create()
	}
		
	public static PatchCli create() {
		System.setProperty("logback.configurationFile", new ClassPathResource('logback.xml').path);
		ClassPathResource res = new ClassPathResource('apscli.properties')
		assert res.exists() : "apscli.properties doesn't exist or is not accessible!"
		ConfigObject conf = new ConfigSlurper(profile).parse(res.URL);
		revisionFilePath = conf.revision.file.path
		defaultHost = conf.host.default
		configDir = conf.config.dir
		dryRunOnClone = conf.onclone.delete.artifact.dryrun
		
		def patchCli = new PatchCli()
		return patchCli
	}

	private PatchCli() {
		super();
	}
	
	def validComponents = ["db", "aps", "mockdb", "nil"]
	def static defaultHost
	// TODO (che,19.4) better a common directory for apg-patch-service? To be discussed
	//def linuxConfigDir = 'file:///var/opt/apg-patch-common'
	def targetSystemMappings
	def validToStates
	def static configDir
	def defaultConfig
	def static revisionFilePath
	def static profile = "production"
	def static dryRunOnClone = true
	
	def validate = true

	def process(def args) {
		println args
		def cmdResults = new Expando();
		cmdResults.returnCode = 1
		cmdResults.results = [:]
		def options = validateOpts(args)
		if (!options) {
			cmdResults.returnCode = 0
			return cmdResults
		}
		try {
			def patchClient = new PatchServiceClient(!options.u ? defaultHost : options.u)
			if (options.l) {
				def result = uploadPatchFiles(options,patchClient)
				cmdResults.results['l'] = result
			}
			if (options.d) {
				def result = downloadPatchFiles(options, patchClient)
				cmdResults.results['d'] =  result
			}
			if (options.e) {
				def result = patchExists(options,patchClient)
				cmdResults.results['e'] =  result
			}
			if (options.fs) {
				def result = findById(options, patchClient)
				cmdResults.results['f'] = result
			}
			if (options.a) {
				def result = findAndPrintAllPatchIds(patchClient)
				cmdResults.results['a'] =  result
			}
			if (options.r) {
				def result = removePatch(options,patchClient)
				cmdResults.results['r'] =  result
			}
			if (options.s) {
				def result = uploadPatch(options,patchClient)
				cmdResults.results['s']=  result
			}
			if (options.sa) {
				def result = savePatch(options,patchClient)
				cmdResults.results['sa']=  result
			}
			if (options.dd) {
				def result = downloadDbModules(options,patchClient)
				cmdResults.results['dd'] = result
			}
			if (options.ud) {
				def result = uploadDbModules(options,patchClient)
				cmdResults.results['ud'] = result
			}
			if (options.dm) {
				def result = downloadServiceMetaData(options,patchClient)
				cmdResults.results['dm'] = result
			}
			if (options.um) {
				def result = uploadServiceMetaData(options,patchClient)
				cmdResults.results['um'] = result
			}
			if (options.sta) {
				def result = stateChangeAction(options,patchClient)
				cmdResults.results['sta'] = result
			}
			if (options.la) {
				def result = listAllFiles(options,patchClient)
				cmdResults.results['la'] = result
			}
			if (options.lf) {
				def result = listFiles(options,patchClient)
				cmdResults.results['lf'] = result
			}
			if (options.oc) {
				def result = onClone(options,patchClient)
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
			if (options.rtr) {
				def result = removeAllTRevisions(options)
				cmdResults.results['rtr'] = result
			}
			if (options.pr) {
				def result = retrieveLastProdRevision()
				cmdResults.results['pr'] = result
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
			oc longOpt: 'onclone', args:1, argName: 'target', 'Clean Artifactory Repo and reset Revision file while cloning', required: false
			sr longOpt: 'saveRevision', args:3, valueSeparator: ",", argName: 'targetInd,installationTarget,revision', 'Save revision file with new value for a given target', required: false
			rr longOpt: 'retrieveRevision', args:2, valueSeparator: ",", argName: 'targetInd,installationTarget', 'Update revision with new value for given target', required: false
			rtr longOpt: 'removeTRevisions', args:1, argName: 'dryRun', 'Remove all T Revision from Artifactory. dryRun=1 -> simulation only, dryRun=0 -> artifact will be deleted', required: false
			pr longOpt: 'prodRevision', args:0, 'Retrieve last revision for the production target', required: false
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
		if (error) {
			cli.usage()
			return null
		}
		options
	}

	def valdidateAndLoadConfigFiles() {
		// TODO (che, 1.5 ) Make File name Configurable
		def targetSystemFile = new File(configDir, "TargetSystemMappings.json")
		def jsonSystemTargets = new JsonSlurper().parseText(targetSystemFile.text)
		targetSystemMappings = [:]
		jsonSystemTargets.targetSystems.find( { a ->  a.stages.find( { targetSystemMappings.put("${a.name}${it.toState}".toString(),"${it.code}") })} )
		//println "Running with TargetSystemMappings: "
		//println JsonOutput.prettyPrint(targetSystemFile.text)
		// TODO validate
		validToStates = targetSystemMappings.keySet()
		def jdbcConfigFule = new File(configDir, "jdbc.groovy")
		defaultConfig = new ConfigSlurper().parse(jdbcConfigFule.toURI().toURL())
		// TODO validate
	}

	def stateChangeAction(def options, def patchClient) {
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
			dbcli.executeStateTransitionAction(defaultConfig, patchNumber, toState)
		} else {
			println "Skipping State change Processing for ${patchNumber}"
		}
		return cmdResult
	}

	def findById(def options, def patchClient) {
		def cmdResult = new Expando()
		def patchNumber = options.fs[0]
		def dirName = options.fs[1]
		def found = retrieveAndWritePatch(patchNumber, dirName, patchClient)
		cmdResult.patchNumber = patchNumber
		cmdResult.dirName = dirName
		cmdResult.exists = found
		return cmdResult
	}

	def downloadPatchFiles(def options, def patchClient) {
		def cmdResult = new Expando()
		List<String> ids =  patchClient.findAllPatchIds()
		ids.each { id ->
			retrieveAndWritePatch(id,options.d, patchClient)
		}
		cmdResult.patchNumbers = ids
		cmdResult.directory = options.d
		return cmdResult
	}

	def findAndPrintAllPatchIds(def patchClient) {
		List<String> ids =  patchClient.findAllPatchIds()
		println "All Patch Ids: ${ids}"
		def cmdResult = new Expando()
		cmdResult.patchNumbers = ids
	}

	def listAllFiles(def options,def patchClient) {
		List<String> files =  patchClient.listAllFiles()
		println "All Files on server: ${files}"
		def cmdResult = new Expando()
		cmdResult.files = files
	}

	def listFiles(def options,def patchClient) {
		List<String> files =  patchClient.listFiles(options.lf)
		println "Files with ${options.lf} as prefix on server: ${files}"
		def cmdResult = new Expando()
		cmdResult.files = files
	}

	def patchExists(def options, def patchClient) {
		def exists = patchClient.patchExists(options.e)
		println "Patch ${options.e} exists is: ${exists} "
		def cmdResult = new Expando()
		cmdResult.exists = exists
		return cmdResult
	}

	def savePatch(def options, def patchClient) {
		patchClient.save(new File(options.sa), Patch.class)
		def cmdResult = new Expando()
		cmdResult.patchFile = options.sa
		return cmdResult
	}


	def uploadPatch(def options, def patchClient) {
		patchClient.savePatch(new File(options.s), Patch.class)
		def cmdResult = new Expando()
		cmdResult.patchFile = options.s
		return cmdResult
	}


	def removePatch(def options, PatchServiceClient patchClient) {
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

	def retrieveAndWritePatch(def id, def file, def patchClient) {
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

	def uploadPatchFiles(def options, def patchClient) {
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

	def downloadDbModules(def options, def patchClient) {
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

	def uploadDbModules(def options, def patchClient) {
		println "Uploading Dbmodules from ${options.ud}"
		ObjectMapper mapper = new ObjectMapper();
		def dbModules = mapper.readValue(new File("${options.ud}"), DbModules.class)
		patchClient.saveDbModules(dbModules)
	}

	def downloadServiceMetaData(def options, def patchClient) {
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

	def uploadServiceMetaData(def options, def patchClient) {
		println "Uploading ServiceMetaData from ${options.um}"
		ObjectMapper mapper = new ObjectMapper();
		def serviceMetaData = mapper.readValue(new File("${options.um}"), ServicesMetaData.class)
		patchClient.saveServicesMetaData(serviceMetaData)
	}


	def validateArtifactNamesForVersion(def options, PatchServiceClient patchClient) {
		println("Validating all Artifact names for version ${options.vvs[0]} on branch ${options.vvs[1]}")
		def invalidArtifacts = patchClient.invalidArtifactNames(options.vvs[0],options.vvs[1])
		println invalidArtifacts
	}

	def onClone(def options, PatchServiceClient patchClient) {
		println "Performing onClone for ${options.ocs[0]}"
		// TODO JHE: get the path to Revision file from a configuration file, or via parameter on command line?
		// 			 will/should be improved as soon as JAVA8MIG-363 will be done.
		def onCloneClient = new PatchCloneClient(revisionFilePath,"${configDir}/TargetSystemMappings.json")
		onCloneClient.onClone(options.ocs[0])
	}

	def retrieveRevisions(def options) {

		def targetInd = options.rrs[0]
		def installationTarget = options.rrs[1]
		
		LOGGER.info("(retrieveRevisions) Retrieving revision for target ${installationTarget} (${targetInd})")

		def revisionFile = new File(revisionFilePath)
		def currentRevision = [P:1,T:10000]
		def lastRevision = [:]
		def revisions = [lastRevisions:lastRevision, currentRevision:currentRevision]
		def patchRevision
		def patchLastRevision
		if (revisionFile.exists()) {
			revisions = new JsonSlurper().parseText(revisionFile.text)
			LOGGER.info("(retrieveRevisions) Revision file exist and following content has been parsed: ${revisions}")
		}

		if(targetInd.equals("P")) {
			patchRevision = revisions.currentRevision[targetInd]
			LOGGER.info("(retrieveRevisions) targetInd = P ... patchRevision = ${patchRevision}")
		}
		else {
			if(revisions.lastRevisions.get(installationTarget) == null) {
				patchRevision = revisions.currentRevision[targetInd]
				LOGGER.info("(retrieveRevisions) Revision for ${installationTarget} was null, patchRevision will be ${patchRevision}")
			}
			else {
				def currentLastRevision = revisions.lastRevisions.get(installationTarget) 
				LOGGER.info("(retrieveRevisions) currentLastRevision = ${currentLastRevision}")
				if(currentLastRevision.endsWith("@P")) {
					patchRevision = currentLastRevision.substring(0, currentLastRevision.size()-2)
					patchLastRevision = "CLONED"
					LOGGER.info("(retrieveRevisions) New patch for ${installationTarget} after clone")
					LOGGER.info("(retrieveRevisions) patchRevision = ${patchRevision} / patchLastRevision = ${patchLastRevision}")
				} 
				else { 
					patchRevision = (revisions.lastRevisions.get(installationTarget)).toInteger() + 1
					LOGGER.info("(retrieveRevisions) patchRevision = ${patchRevision}")
				}
			}
		}
		
		if(patchLastRevision == null) {
			patchLastRevision = revisions.lastRevisions.get(installationTarget,'SNAPSHOT')
		}

		LOGGER.info("(retrieveRevisions) patchLastRevision = ${patchLastRevision}")
		// JHE (31.05.2018) : we print the json on stdout so that the pipeline can get and parse it. Unfortunately there is currently no supported alternative: https://issues.jenkins-ci.org/browse/JENKINS-26133
		def json = JsonOutput.toJson([fromRetrieveRevision:[revision: patchRevision, lastRevision: patchLastRevision]])
		LOGGER.info("(retrieveRevisions) Following json will be returned : ${json}")
		println json
	}
	
	def retrieveLastProdRevision() {
		def targetSystemFile = new File(configDir, "TargetSystemMappings.json")
		def jsonSystemTargets = new JsonSlurper().parseText(targetSystemFile.text)
		def prodTarget 
		jsonSystemTargets.targetSystems.each{ target ->
			if(target.typeInd == "P") {
				prodTarget = target.target 
			}
		}
		def revisions = new JsonSlurper().parseText(new File(revisionFilePath).text)
		def prodRev = revisions.lastRevisions[prodTarget]
		
		// JHE (15.06.2018) : we print the json on stdout so that the pipeline can get and parse it. Unfortunately there is currently no supported alternative: https://issues.jenkins-ci.org/browse/JENKINS-26133
		def json = JsonOutput.toJson([lastProdRevision:prodRev])
		println json
	}

	def saveRevisions(def options) {

		def targetInd = options.srs[0]
		def installationTarget = options.srs[1]
		def revision = options.srs[2]
		
		LOGGER.info("(saveRevisions) Saving revisions for targetInd=${targetInd}, installationTarget=${installationTarget}, revision=${revision}")

		def revisionFile = new File(revisionFilePath)
		def currentRevision = [P:1,T:10000]
		def lastRevision = [:]
		def revisions = [lastRevisions:lastRevision, currentRevision:currentRevision]
		if (revisionFile.exists()) {
			revisions = new JsonSlurper().parseText(revisionFile.text)
			LOGGER.info("(saveRevisions) Current revisions are: ${revisions}")
		}
		if(targetInd.equals("P")) {
			LOGGER.info("(saveRevisions) Increasing Prod revision ...")
			revisions.currentRevision[targetInd]++
		}
		else {
			// We increase it only when saving a new Target
			if(revisions.lastRevisions.get(installationTarget) == null) {
				revisions.currentRevision[targetInd] = revisions.currentRevision[targetInd] + 10000
				LOGGER.info("(saveRevisions) ${installationTarget} was new, next test range revision will start at ${revisions.currentRevision[targetInd]}")
			}
		}
		revisions.lastRevisions[installationTarget] = revision
		LOGGER.info("(saveRevisions) Following revisions will be save: ${revisions}")
		new File(revisionFilePath).write(new JsonBuilder(revisions).toPrettyString())

	}

	def removeAllTRevisions(def options) {
		println "Removing all T Artifact from Artifactory."
		boolean dryRun = true
		if(options.rtr.size() > 0) {
			if(options.rtr[0] == "0") {
				dryRun = false
			}
		}
		// TODO JHE: get the path to Revision file from a configuration file, or via parameter on command line?
		// 			 will/should be improved as soon as JAVA8MIG-363 will be done.
		def cloneClient = new PatchCloneClient(revisionFilePath,"${configDir}/TargetSystemMappings.json")
		cloneClient.deleteAllTRevisions(dryRun)
	}
}