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
		
		println "apsRevCli running with ${profile} profile"
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
			if (options.sr) {
				def result = saveRevisions(options)
				cmdResults.results['sr'] = result
			}
			if (options.rr) {
				def result = retrieveRevisions(options)
				cmdResults.results['rr'] = result
			}
			if (options.pr) {
				def result = retrieveLastProdRevision()
				cmdResults.results['pr'] = result
			}
			if (options.resr) {
				def result = resetRevision(options)
				cmdResults.results['resr'] = result
			}
			// TODO JHE (26.06.2018): will be removed with JAVA8MIG-389
			if (options.rtr) {
				def result = removeAllTRevisions(options)
				cmdResults.results['rtr'] = result
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
	
	def saveRevisions(def options) {
		def patchRevisionClient = new PatchRevisionClient(config)
		patchRevisionClient.saveRevisions(options.srs[0],options.srs[1],options.srs[2])
	}
	
	def retrieveRevisions(def options) {
		def patchRevisionClient = new PatchRevisionClient(config)
		patchRevisionClient.retrieveRevisions(options.rrs[0],options.rrs[1])
	}
	
	def retrieveLastProdRevision() {
		def patchRevisionClient = new PatchRevisionClient(config)
		patchRevisionClient.retrieveLastProdRevision()
	}
	
	def resetRevision(def options) {
		def patchRevisionClient = new PatchRevisionClient(config)
		def target = options.resrs[0]
		patchRevisionClient.resetLastRevision(target)
	}
	
	// TODO JHE (26.06.2018): will be removed with JAVA8MIG-389
	def removeAllTRevisions(def options) {
		def patchArtifactoryClient = new PatchArtifactoryClient(config)
		def dryRun = true
		if(options.rtrs[0] == "0") {
			dryRun = false
		}
		patchArtifactoryClient.deleteAllTRevisions(dryRun)
	}
	
	private def validateOpts(def args) {
		def cli = new CliBuilder(usage: 'apsrevpli.sh -[h|sr|rr|pr|resr]')
		cli.formatter.setDescPadding(0)
		cli.formatter.setLeftPadding(0)
		cli.formatter.setWidth(100)
		cli.with {
			// TODO (CHE, 13.9) Factor out Revision Operations into Interface
			h longOpt: 'help', 'Show usage information', required: false
			sr longOpt: 'saveRevision', args:3, valueSeparator: ",", argName: 'targetInd,installationTarget,revision', 'Save revision file with new value for a given target', required: false
			rr longOpt: 'retrieveRevision', args:2, valueSeparator: ",", argName: 'targetInd,installationTarget', 'Update revision with new value for given target', required: false
			pr longOpt: 'prodRevision', args:0, 'Retrieve last revision for the production target', required: false
			resr longOpt: 'resetRevision', args:1, argName: 'target', 'Reset revision number for a given target', required: false
			// TODO JHE (26.06.2018): will be removed with JAVA8MIG-389
			rtr longOpt: 'removeTRevisions', args:1, argName: 'dryRun', 'Remove all T Revision from Artifactory. dryRun=1 -> simulation only, dryRun=0 -> artifact will be deleted', required: false
		}
		
		def options = cli.parse(args)
		def error = false;

		if (options == null) {
			println "Wrong parameters"
			cli.usage()
			return null
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
		if (options.resr) {
			if(options.resrs.size() != 1) {
				println "target parameter is required when reseting revision."
				error = true
			}
		}
		// TODO JHE (26.06.2018): will be removed with JAVA8MIG-389
		if (options.rtr) {
			if(options.rtrs.size() != 1) {
				println "No parameter has been set, only a dryRun will be done. To delete all T artifact, please explicitely set dryRun to 0."
				error = true
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
