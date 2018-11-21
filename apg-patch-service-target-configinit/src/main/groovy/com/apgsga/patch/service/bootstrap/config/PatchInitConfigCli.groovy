package com.apgsga.patch.service.bootstrap.config

import org.codehaus.groovy.runtime.ExceptionUtils

import groovy.sql.Sql.CreateCallableStatementCommand

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
		
		def cmdResults = new Expando()
		cmdResults.returnCode = 1
		def options = validateOpts(args)
		
		if (!options) {
			cmdResults.returnCode = 0
			return cmdResults
		}
	
		try {
			if(options.i) {
				cmdResults.result = initConfiguration(options)
			} else if (options.sta) {
				def patchNumber = options.stas[0]
				def toState = options.stas[1]
				cmdResults.dbResult = dbCli.executeStateTransitionAction(patchNumber,toState)
			}
			cmdResults.returnCode = 0
			return cmdResults
			
		} catch (AssertionError e) {
			System.err.println "Client Error ccurred ${e.message} "
			return cmdResults
		} catch (Exception e) {
			System.err.println "Unhandeled Exception occurred "
			System.err.println e.toString()
			return cmdResults
		}
	}
	
	private def initConfiguration(def options) {
		def initConfigFile = options.is[0]
		println "Provided configuration file was : ${initConfigFile}"
		def initConfig = parseConfig(initConfigFile)
		def initClient = new PatchInitConfigClient(initConfig)
		initClient.initAll()
	}
	
	private def parseConfig(def initConfigFile) {
		ConfigSlurper cs = new ConfigSlurper()
		def props = new Properties()
		new File(initConfigFile).withInputStream{ stream -> 
			props.load(stream)	
		}
		cs.parse(props)
	}
 

	private def validateOpts(def args) {
		def cli = new CliBuilder(usage: 'patchinitcli.sh -[h|i|]')
		cli.formatter.setDescPadding(0)
		cli.formatter.setLeftPadding(0)
		cli.formatter.setWidth(100)
		cli.with {
			h longOpt: 'help', 'Show usage information', required: false
			i longOpt: 'init', args:1, argName: 'fileName', 'Location of config Property File', required: false
		}

		def options = cli.parse(args)
		def error = true

		if (!options) {
			return null
		}
		
		if(options.size)

		if(options.h) {
			cli.usage()
			return null
		}

		if(options.i) {
			error = false
		}
	
		if(error) {
			cli.usage()
			return null
		}

		options
	}
}
