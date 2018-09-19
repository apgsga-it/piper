package com.apgsga.patch.service.client.db

import org.codehaus.groovy.runtime.StackTraceUtils
import org.springframework.core.io.ClassPathResource

import com.apgsga.patch.service.client.PatchClientServerException

import groovy.sql.Sql

class PatchDbCli {

	def config

	private PatchDbCli() {
	}

	public static create() {
		return new PatchDbCli()
	}

	def process(def args) {

		println "apsDbCli running with ${profile} profile"
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
			def jdbcConfigFile = new File(config.ops.groovy.file.path)
			def defaultJdbcConfig = new ConfigSlurper().parse(jdbcConfigFile.toURI().toURL())
			def dbConnection = Sql.newInstance(defaultJdbcConfig.db.url, defaultJdbcConfig.db.user, defaultJdbcConfig.db.passwd)
			def dbCli = new PatchDbClient(dbConnection, null)
			if(options.lpac) {
				def status = options.lpacs[0]
				def result = dbCli.listPatchAfterClone(status,config.postclone.list.patch.file.path)
				cmdResults.results['lpac'] = result
			} else if (options.rsta) {
				def result = dbCli.retrieveCurrentPatchState(options.rsta[0])
				cmdResults.results['rsta'] = result
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

	private def validateOpts(def args) {
		def cli = new CliBuilder(usage: 'apsdbpli.sh -[h|lpac]')
		cli.formatter.setDescPadding(0)
		cli.formatter.setLeftPadding(0)
		cli.formatter.setWidth(100)
		cli.with {
			h longOpt: 'help', 'Show usage information', required: false
			lpac longOpt: 'listPatchAfterClone', args:1, argName: 'status', 'Get list of patches to be re-installed after a clone', required: false
			rsta longOpt: 'retrievePatchStatus', args:1, argName: 'patchNumber', 'Get the Status for the Patch with PatchNumber', required: false
			nsta longOpt: 'notifyPatchStatus', args:1, argName: 'patchNumber', 'Notify the Patch Status back to the Db', required: false
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
		
		if(options.rsta) {
			if(options.rsta.size() != 1) {
				println("Target status is required when fetching list of patch to be re-installed.")
				error = true
			}
			if (!options.rsta.isInteger()) {
				println "Patchnumber ${options.rsta} is not a Integer"
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
