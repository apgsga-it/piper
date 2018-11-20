package com.apgsga.patch.service.bootstrap.config

import org.apache.commons.lang.exception.ExceptionUtils


/**
 * This command line Tool, is used for an intial setup of a cloned target Plattform
 * It configures the target relevant Configuration Properties of an Installation
 * It should'nt be run on a productive System.
 *
 */
public class PatchInitConfigCli {

	def config

	private PatchInitConfigCli() {
	}

	public static create() {
		return new PatchInitConfigCli()
	}

	def process(def args) {
		def cmdResults = new Expando();
		cmdResults.returnCode = 1
		def options = validateOpts(args)
		if (!options) {
			cmdResults.returnCode = 0
			return cmdResults
		}
		try {
			def initConfig = new PatchInitConfigService(config)
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
		}  catch (AssertionError e) {
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
			cf longOpt: 'configFile', args:1, argName: 'fileName', 'Location of config Property File', required: yes
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

		if(options.cf) {
			
		}

	
		if(error) {
			cli.usage()
			return null
		}

		options
	}
}
