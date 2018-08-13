package com.apgsga.patch.service.client.db

import org.codehaus.groovy.runtime.StackTraceUtils
import org.springframework.core.io.ClassPathResource

import com.apgsga.patch.service.client.PatchClientServerException

import groovy.json.JsonBuilder
import groovy.json.JsonOutput
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
			if(options.lpac) {
				def status = options.lpacs[0]
				def result = listPatchAfterClone(status)
				cmdResults.results['lpac'] = result
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
	
	private def listPatchAfterClone(def status) {
		//Query db to get list of patch to be installed fora given status
		def jdbcConfigFile = new File(config.ops.groovy.file.path)
		def defaultJdbcConfig = new ConfigSlurper().parse(jdbcConfigFile.toURI().toURL())
		// If we don't force sql to be a String, ${status} will be replace with a "?" at the time we run the query.
		def String sql = "SELECT id FROM cm_patch_install_sequence_f WHERE ${status}=1 AND (produktion = 0 OR chronology > trunc(SYSDATE))"
		def dbConnection = Sql.newInstance(defaultJdbcConfig.db.url, defaultJdbcConfig.db.user, defaultJdbcConfig.db.passwd)
		def patchNumbers = []
		dbConnection.eachRow(sql) { row ->
			def rowId = row.ID
			println "Patch ${rowId} added to the list of patch to be re-installed."
			patchNumbers.add(rowId)
		}
		
		def listPatchFile = new File(config.postclone.list.patch.file.path)
		
		if(listPatchFile.exists()) {
			listPatchFile.delete()
		}
		
		listPatchFile.write(new JsonBuilder(patchlist:patchNumbers).toPrettyString())
		
		return patchNumbers
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
		// TODO JHE: Add oc, sr, rr and rtr description here.
		def cli = new CliBuilder(usage: 'apsdbpli.sh -[h|lpac]')
		cli.formatter.setDescPadding(0)
		cli.formatter.setLeftPadding(0)
		cli.formatter.setWidth(100)
		cli.with {
			h longOpt: 'help', 'Show usage information', required: false
			lpac longOpt: 'listPatchAfterClone', args:1, argName: 'status', 'Get list of patches to be re-installed after a clone', required: false
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
		
		if(error) {
			cli.usage()
			return null
		}
		
		options
		
	}
}
