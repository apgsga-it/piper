package com.apgsga.patch.service.client.db

import com.apgsga.patch.service.client.PatchClientServerException
import com.apgsga.patch.service.client.config.PliConfig
import com.apgsga.patch.service.client.utils.TargetSystemMappings
import groovy.sql.Sql
import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.context.annotation.AnnotationConfigApplicationContext

/**
 * This command line Tool  is used to make jdbc calls to the It21 database. 
 * It is intended to be used in a automated scripting enviroment like Jenkins pipeline, which depends on standard output processing
 * therefore care has been taken to avoid logging via standard output in the normal path of execution
 *
 */
class PatchDbCli {

	def config

	private PatchDbCli() {
	}

	public static create() {
		return new PatchDbCli()
	}

	def process(def args) {
		def context =  new AnnotationConfigApplicationContext(PliConfig.class);
		config = context.getBean(ConfigObject.class);
		TargetSystemMappings.instance.load(config)
		def cmdResults = new Expando();
		cmdResults.returnCode = 1
		def options = validateOpts(args)
		if (!options) {
			cmdResults.returnCode = 0
			return cmdResults
		}
		try {
			def dbConnection = Sql.newInstance(config.db.url, config.db.user, config.db.passwd)
			def dbCli = new PatchDbClient(dbConnection)
			if(options.lpac) {
				def status = options.lpacs[0]
				def filePath = "${config.postclone.list.patch.filepath.template}${status}.json"
				cmdResults.result = dbCli.listPatchAfterClone(status,filePath)
			} else if (options.dbsta) {
				def patchNumber = options.dbstas[0]
				def toState = options.dbstas[1]
				cmdResults.dbResult = dbCli.executeStateTransitionAction(patchNumber,toState)
			} else if (options.cpf) {
				def status = options.cpfs[0]
				def destFolder = options.cpfs[1]
				def pathToPatchFolder = "${config.json.db.location}"
				cmdResults.result = dbCli.copyPatchFile(status,destFolder,pathToPatchFolder)
			}
			cmdResults.returnCode = 0
			return cmdResults
		} catch (PatchClientServerException e) {
			System.err.println "Server Error ccurred on ${e.errorMessage.timestamp} : ${e.errorMessage.errorText} "
			cmdResults.error = e.errorMessage
			return cmdResults
		} catch (AssertionError e) {
			System.err.println "Client Error ccurred ${e.message} "
			println ExceptionUtils.getFullStackTrace(e)
			cmdResults.error = e.message
			return cmdResults
		} catch (Exception e) {
			System.err.println "Unhandeled Exception occurred "
			System.err.println e.toString()
			println ExceptionUtils.getFullStackTrace(e)
			cmdResults.error = e
			return cmdResults
		}
	}


	private def validateOpts(def args) {
		def cli = new CliBuilder(usage: 'apsdbpli.sh -[h|lpac|[rsta,patchNumber]')
		cli.formatter.setDescPadding(0)
		cli.formatter.setLeftPadding(0)
		cli.formatter.setWidth(100)
		cli.with {
			h longOpt: 'help', 'Show usage information', required: false
			lpac longOpt: 'listPatchAfterClone', args:1, argName: 'status', 'Get list of patches to be re-installed after a clone', required: false
			dbsta longOpt: 'dbstateChange', args:2, valueSeparator: ",", argName: 'patchNumber,toState', 'Notfiy State Change for a Patch with <patchNumber> to <toState> to the database', required: false
			cpf longOpt: 'copyPatchFiles', args:2, valueSeparator: ",", argName: "status,destFolder", 'Copy patch files for a given status into the destfolder', required: false
		}

		def options = cli.parse(args)
		def error = false;

		if (!options | options.getOptions().size() == 0) {
			println "No option have been provided, please see the usage."
			cli.usage()
			return null
		}

		if(options.h) {
			cli.usage()
			return null
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
			def toState = options.dbstas[1]
			def validToStates = TargetSystemMappings.instance.get().keySet()
			if (!validToStates.contains(toState) ) {
				println "ToState ${toState} not valid: needs to be one of ${validToStates}"
				error = true
			}
		}

		if(options.cpf) {
			if(options.cpfs.size() != 2) {
				println "status and destFolder are required when copying patch files"
				error = true
			}
		}

		if(error) {
			cli.usage()
			return null
		}

		options
	}
}
