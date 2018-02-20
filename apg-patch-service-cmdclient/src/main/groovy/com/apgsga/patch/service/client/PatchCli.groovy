package com.apgsga.patch.service.client


import com.apgsga.microservice.patch.api.DbModules
import com.apgsga.microservice.patch.api.Patch
import com.apgsga.microservice.patch.api.ServiceMetaData
import com.apgsga.microservice.patch.api.ServicesMetaData
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

class PatchCli {

	public static PatchCli create() {
		return new PatchCli()
	}

	private PatchCli() {
		super();
	}
	def validToStates = ["EntwicklungInstallationsbereit","Informatiktestinstallation","Produktionsinstallation", "Entwicklung"]
	
	def process(def args) {
		def options = validateOpts(args)
		if (!options) return
		def cmdResults = new Expando();
		cmdResults.results = [:]
		cmdResults.returnCode = 1
		def patchClient = new PatchServiceClient(options.u)
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
			def result = savePatch(options,patchClient)
			cmdResults.results['s']=  result
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
		cmdResults.returnCode = 0
		return cmdResults
	}
	def validateOpts(args) {
		def cli = new CliBuilder(usage: 'pli.groovy -u <url> -[h] -[[l|d|dd|dm] <directory>]')
		cli.with {
			h longOpt: 'help', 'Show usage information', required: false
			u longOpt: 'host', args:1 , argName: 'hostBaseUrl', 'The Base Url of the Patch Service', required: false
			l longOpt: 'upload', args:1 , argName: 'directory', 'Upload all Patch files from this <directory> to the Patch Service', required: false
			d longOpt: 'download', args:1 , argName: 'directory', 'Download all Patch files from the Patch Service to a directory', required: false
			e longOpt: "exists", args:1, argName: 'patchNumber', 'Print true resp. false if Patch of the <patchNumber> exists or not', required: false
			f longOpt: 'findById', args:2, valueSeparator: ",", argName: 'patchNumber,directory','Retrieve a Patch with the <patchNumber> to a <directory>', required: false
			a longOpt: 'findAllIds','Retrieve and print all PatchIds', required: false
			r longOpt: 'remove', args:1, argName: 'patchNumber', 'Remove Patch with <patchNumber>', required: false
			s longOpt: 'save', args:1, argName: 'patchFile', 'Uploads a <patchFile> to the server', required: false
			dd longOpt: 'downloadDbmodules', args:1, argName: 'directory', 'Download Dbmodules from server to <directory>', required: false
			ud longOpt: 'uploadDbmodules', args:1, argName: 'file', 'Upload Dbmodules from <file> to server', required: false
			dm longOpt: 'downloadServicesMeta', args:1, argName: 'directory', 'Download ServiceMetaData from server to <directory>', required: false
			um longOpt: 'uploadServicesMeta', args:1, argName: 'file', 'Upload ServiceMetaData from <file> to server', required: false
			sta longOpt: 'stateChange', args:2, valueSeparator: ",", argName: 'patchNumber,toState', 'Start State Change for a Patch with <patchNumber> to <toState>', required: false
		}

		def options = cli.parse(args)
		def error = false;
		if (!options | options.h) {
			cli.usage()
			println "Valid toStates are: ${validToStates}"
			return null
		}
		if (!options.u) {
			println "Patch Service Base Url must be provided"
			error = true;
		}
		if (options.l) {
			def directory = new File(options.l)
			if (!directory.exists() | !directory.directory) {
				println "Directory ${options.l} not valid: either not a directory or it doesn't exist"
				error = true
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
		if (options.stas) {
			def patchNumber = options.stas[0]
			if (!patchNumber.isInteger()) {
				println "Patchnumber ${patchNumber} is not a Integer"
				error = true
			}
			def toState = options.stas[1]
			if (!validToStates.contains(toState) ) {
				println "ToTate ${toState} not valid: needs to be one of ${validToStates}"
				error = true
			}
		}
		if (options.e) {
			if (!options.e.isInteger()) {
				println "Patchnumber ${options.e} is not a Integer"
				error = true
			}
		}
		if (error) {
			cli.usage()
			return null
		}
		options
	}
	
	def stateChangeAction(def options, def patchClient) {
		def cmdResult = new Expando()
		def patchNumber = options.stas[0]
		def toState = options.stas[1]
		cmdResult.patchNumber = patchNumber
		cmdResult.toState = toState
		patchClient.executeStateTransitionAction(patchNumber,toState)
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
		cmdResult.directory = option.d
		return cmdResult
	}

	def findAndPrintAllPatchIds(def patchClient) {
		List<String> ids =  patchClient.findAllPatchIds()
		println "All Patch Ids: ${ids}"
		def cmdResult = new Expando()
		cmdResult.patchNumbers = ids
	}

	def patchExists(def options, def patchClient) {
		def exists = patchClient.patchExists(options.e)
		println "Patch ${options.e} exists is: ${exists} "
		def cmdResult = new Expando()
		cmdResult.exists = exists
		return cmdResult
	}


	def savePatch(def options, def patchClient) {
		patchClient.save(new File(options.s), Patch.class)
		def cmdResult = new Expando()
		cmdResult.patchFile = options.s
		return cmdResult
	}


	def removePatch(def options, def patchClient) {
		println "Reading: ${options.r} to remove from server"
		def patchData = patchClient.findById(options.r)
		println "Removing Patch ${options.r}"
		patchClient.remove(patchData)
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
			patchClient.save(file, Patch.class)
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
		patchClient.save(dbModules)
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
}