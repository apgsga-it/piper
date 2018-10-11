package com.apgsga.patch.service.client.db

import org.apache.commons.lang.exception.ExceptionUtils
import org.codehaus.groovy.runtime.StackTraceUtils
import org.springframework.core.io.ClassPathResource

import com.apgsga.patch.service.client.utils.TargetSystemMappings
import com.apgsga.patch.service.client.PatchClientServerException

import groovy.sql.Sql

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
		config = parseConfig()
		TargetSystemMappings.instance.load(config)
		def cmdResults = new Expando();
		cmdResults.returnCode = 1
		def options = validateOpts(args)
		if (!options) {
			cmdResults.returnCode = 0
			return cmdResults
		}
		try {
			def jdbcConfigFile = new File(config.ops.groovy.file.path)
			def defaultJdbcConfig = new ConfigSlurper().parse(jdbcConfigFile.toURI().toURL())
			def dbConnection = Sql.newInstance(defaultJdbcConfig.db.url, defaultJdbcConfig.db.user, defaultJdbcConfig.db.passwd)
			def dbCli = new PatchDbClient(dbConnection, config)
			if(options.lpac) {
				def status = options.lpacs[0]
				cmdResults.result = dbCli.listPatchAfterClone(status,config.postclone.list.patch.file.path)
			} else if (options.sta) {
				def patchNumber = options.stas[0]
				def toState = options.stas[1]
				cmdResults.dbResult = dbCli.executeStateTransitionAction(patchNumber,toState)
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

	private def validateOpts(def args) {
		def cli = new CliBuilder(usage: 'apsdbpli.sh -[h|lpac|[rsta,patchNumber]')
		cli.formatter.setDescPadding(0)
		cli.formatter.setLeftPadding(0)
		cli.formatter.setWidth(100)
		cli.with {
			h longOpt: 'help', 'Show usage information', required: false
			lpac longOpt: 'listPatchAfterClone', args:1, argName: 'status', 'Get list of patches to be re-installed after a clone', required: false
			sta longOpt: 'stateChange', args:2, valueSeparator: ",", argName: 'patchNumber,toState', 'Notfiy State Change for a Patch with <patchNumber> to <toState> to the database', required: false
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

		if (options.sta) {
			if (options.stas.size() != 2 ) {
				println "Option sta needs 2 arguments: <patchNumber,toState>>"
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
		}

		if(error) {
			cli.usage()
			return null
		}

		options
	}
}
