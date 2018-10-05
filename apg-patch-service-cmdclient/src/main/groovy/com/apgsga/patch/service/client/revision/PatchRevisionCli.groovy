package com.apgsga.patch.service.client.revision

import org.codehaus.groovy.runtime.StackTraceUtils
import org.springframework.core.io.ClassPathResource

import com.apgsga.patch.service.client.PatchArtifactoryClient
import com.apgsga.patch.service.client.PatchClientServerException

class PatchRevisionCli {
	
	def config
	
	private PatchRevisionCli() {
	}
	
	public static create() {
		return new PatchRevisionCli()
	}	

	def process(def args) {
		
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
			
			if (options.ar) {
				def result = addRevision(options)
				cmdResults.results['ar'] = result
			}
			
			if (options.nr) {
				def result = nextRevision()
				cmdResults.results['nr'] = result
			}
			
			if (options.lr) {
				def result = lastRevision(options)
				cmdResults.results['lr'] = result
			}
			
			if(options.rr) {
				def result = resetRevisions(options)
				cmdResults.results['rr'] = result
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
	
	private def lastRevision(def options) {
		def patchRevClient = new PatchRevisionClient(config)
		patchRevClient.lastRevision(options.lrs[0])
	}
	
	private def addRevision(def options) {
		def patchRevClient = new PatchRevisionClient(config)
		patchRevClient.addRevision(options.ars[0],options.ars[1])
	}
	
	private def nextRevision() {
		def patchRevClient = new PatchRevisionClient(config)
		patchRevClient.nextRevision()
	}
	
	private def resetRevisions(def options) {
		def patchRevClient = new PatchRevisionClient(config)
		patchRevClient.resetRevisions(options.rrs[0], options.rrs[1])
	}
	
	private def validateOpts(def args) {
		def cli = new CliBuilder (usage: 'apsrevpli.sh -[h|ar|lr|nr|rr]')
		cli.formatter.setDescPadding(0)
		cli.formatter.setLeftPadding(0)
		cli.formatter.setWidth(100)
		cli.with {
			// TODO (CHE, 13.9) Factor out Revision Operations into Interface
			h longOpt: 'help', 'Show usage information', required: false
			ar longOpt: 'addRevision', args:2, valueSeparator: ",", argName: 'target,revision', 'Add a new revision number to the revision list of the given target', required: false
			lr longOpt: 'lastRevision', args:1, argName: 'target', 'Get last revision for the given target', required: false
			nr longOpt: 'nextRevision', args:0, 'Get the next global revision number', required: false
			rr longOpt: 'resetRevision', args:2, valueSeparator: ",", argName: 'source,target', 'Reset the revision list and last revision for the given target', required: false
			// TODO JHE: to be implemented, probably while working on JAVA8MIG-
			i longOpt: 'initRevision', args:0 , 'Initialize the Revision Tracking', required: false
		}
		
		def options = cli.parse(args)
		def error = true; 
		
		if (!options) {
			return null
		}
		
		if (options.h) {
			cli.usage()
			return null
		}
		
		if (options.nr || options.lr || options.rr || options.llr) {
			error = false
		}

		// TODO JHE: Really need this one ??? Shouldn't it be done within cli.parse ??		
		if(options.ar) {
			if(options.ars.size() != 2) {
				println "-ar option required 2 parameters!"
			}
			else {
				error = false
			}
		}
		
		if (error) {
			cli.usage()
			return null
		}
		
		options
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
}
