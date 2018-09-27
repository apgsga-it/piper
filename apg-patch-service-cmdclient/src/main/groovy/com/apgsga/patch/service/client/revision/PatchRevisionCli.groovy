package com.apgsga.patch.service.client.revision

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
	
//	def saveRevisions(def options) {
//		def patchRevisionClient = new PatchRevisionClient(config)
//		patchRevisionClient.saveRevisions(options.srs[0],options.srs[1],options.srs[2])
//	}
//	
//	def retrieveRevisions(def options) {
//		def patchRevisionClient = new PatchRevisionClient(config)
//		patchRevisionClient.retrieveRevisions(options.rrs[0],options.rrs[1])
//	}
//	
//	def retrieveLastProdRevision() {
//		def patchRevisionClient = new PatchRevisionClient(config)
//		patchRevisionClient.retrieveLastProdRevision()
//	}
//	
//	def resetRevision(def options) {
//		def patchRevisionClient = new PatchRevisionClient(config)
//		def target = options.resrs[0]
//		patchRevisionClient.resetLastRevision(target)
//	}
//	
//	// TODO JHE (26.06.2018): will be removed with JAVA8MIG-389
//	def removeAllTRevisions(def options) {
//		def patchArtifactoryClient = new PatchArtifactoryClient(config)
//		def dryRun = true
//		if(options.rtrs[0] == "0") {
//			dryRun = false
//		}
//		patchArtifactoryClient.deleteAllTRevisions(dryRun)
//	}
	
	private def validateOpts(def args) {
		def cli = new CliBuilder (usage: 'apsrevpli.sh -[h|ar|lr|lpr|spr|nr|rr]')
		cli.formatter.setDescPadding(0)
		cli.formatter.setLeftPadding(0)
		cli.formatter.setWidth(100)
		cli.with {
			// TODO (CHE, 13.9) Factor out Revision Operations into Interface
			h longOpt: 'help', 'Show usage information', required: false
			ar longOpt: 'addRevision', args:2, valueSeparator: ",", argName: 'target,revision', 'Add a new revision number to the revision list of the given target', required: false
			lr longOpt: 'lastRevision', args:1, argName: 'target', 'Get last revision for the given target', required: false
			lpr longOpt: 'lastProdRevision', args:0, 'Get the last Production revision', required: false
			spr longOpt: 'setProductionRevision', args:0, 'Set the last Production revision', required: false
			nr longOpt: 'nextRevision', args:0, 'Get the next global revision number', required: false
			rr longOpt: 'resetRevision', args:1, argName: 'target', 'Reset the revision list and last revision for the given target', required: false
			
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
		
		if (options.nr || options.lr) {
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
