package com.apgsga.patch.service.bootstrap.config

import groovy.io.FileType
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.util.slurpersupport.NodeChild
import groovy.xml.XmlUtil

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import com.apgsga.patch.service.configinit.util.ConfigInitUtil

class PatchInitConfigClient {
	
	ConfigObject initConfig
	
	public PatchInitConfigClient(def initConfig) {
		this.initConfig = initConfig
	}
	
	def initAll() {
		println "init all started ... "

		initTargetSystemMapping()
		initPiperProperties()
		initJenkinsConfig()
		initMavenSettings()
		initGradleSettings()
				
	}
	
	def initJenkinsConfig() {
		println "Initialisation of Jenkins config.xml started ..."
		backupFile(initConfig.jenkins.jenkinsConfigFileLocation)
		adaptJenkinsConfig()		
		println "Initialisation of Jenkins config.xml done!"
	}

	def initTargetSystemMapping() {
		println "Initialisation of targetSystemMapping started ..."
		backupFile(initConfig.targetSystemMappings)
		changeTargetSystemMappingContent()
		println "Initialisation of targetSystemMapping done!"
	}

	def initPiperProperties() {
		println "Initialisation of patch service properties started ..."
		
		// TODO JHE: get dir path from init property file
		def dir = new File("src/test/resources/etc/opt")
		//TODO JHE: get ".properties" from init property file
		dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.properties/) {
			backupFile(it.getPath())
			adaptContentForPiperPropertiesFile(it)
		}
		
		println "Initialisation of patch service properties done!"
		
	}
	
	def adaptContentForPiperPropertiesFile(File file) {
		
		
		println "Process file : " + file.getPath()
		
		def piperPropertiesFromInitConfig = initConfig.flatten()
		Properties propsToBeUpdated = new Properties()
		file.withInputStream{ stream ->
			propsToBeUpdated.load(stream)
		}
		
		piperPropertiesFromInitConfig.forEach({key,value -> 
			println "init key = ${key} , init value = ${value}"
		})

		println "======================================================================================="
		println "======================================================================================="		

		def needToUpdateFile = false
		Properties newProps = new Properties()
		propsToBeUpdated.each({key,value -> 
			
			//println "key = ${key} , value = ${value}"
			
			def String searchedKey = "piper.${key}"
			println "piper key = piper.${key}"
			if(piperPropertiesFromInitConfig.containsKey(searchedKey)) {
				def newValue = piperPropertiesFromInitConfig.get(searchedKey)
				println "${key} will be updated with: ${newValue}"
				newProps.put(key, newValue)
				needToUpdateFile = true
			}
			
		})

		if(needToUpdateFile) {
			propsToBeUpdated.putAll(newProps)
			
			PrintWriter pw = new PrintWriter(file)
			pw.write("")
			
			propsToBeUpdated.each({key,value -> 
				pw.write("${key}=${value}")
				pw.write(System.getProperty("line.separator"))
			})

			pw.close()			
//			propsToBeUpdated.store(file.newWriter(),null)
		}
		
		
		
		println "done"
		
	}
	
	def initMavenSettings() {
		println "Initialisation of maven settings started ..."
	}
	
	def initGradleSettings() {
		println "Initialisation of graddle settings started ..."
	}
	
	private def adaptJenkinsConfig() {
		
		def jenkinsConfig = new XmlSlurper().parse(new File(initConfig.jenkins.jenkinsConfigFileLocation))
		
		
		jenkinsConfig.numExecutors = 5

		boolean iscvsFwRootOnNextIter = false
		boolean iscvsRootOnNextIter = false
		boolean isRepoRoPasswdOnNextIter = false
		
		// JHE: Well, rather bad to iterate like below ... but we need to deal with such a list:
		/*
		 *<string>ARTIFACTORY_SERVER_ID</string>
          <string>artifactory4t4apgsga</string>
          <string>CVS_FW_ROOT</string>
          <string>:ext:svcCvsClient@cvs.apgsga.ch:/var/local/cvs/root</string>
          <string>CVS_ROOT</string>
          <string>:ext:svcCvsClient@cvs.apgsga.ch:/var/local/cvs/root</string>
          <string>CVS_RSH</string>
          <string>ssh</string>
          <string>GITHUB_JENKINS_VERSION</string>
          <string>refs/heads/1.0.x</string>
		 * 
		 */
		jenkinsConfig.globalNodeProperties."hudson.slaves.EnvironmentVariablesNodeProperty".envVars."tree-map".string.each({NodeChild p ->

			if(iscvsFwRootOnNextIter) {
				p.replaceBody(initConfig.jenkins.cvsFwRoot)
				iscvsFwRootOnNextIter = false
			}
			
			if(iscvsRootOnNextIter) {
				p.replaceBody(initConfig.jenkins.cvsRoot)
				iscvsRootOnNextIter = false
			}
			
			if(isRepoRoPasswdOnNextIter) {
				p.replaceBody(initConfig.jenkins.repo_ro_password)
				isRepoRoPasswdOnNextIter = false
			}
			
			if(p.equals("CVS_FW_ROOT")) {
				iscvsFwRootOnNextIter = true
			}
			
			if(p.equals("CVS_ROOT")) {
				iscvsRootOnNextIter = true
			}
			
			if(p.equals("REPO_RO_PASSWD")) {
				isRepoRoPasswdOnNextIter = true
			}
		})
		
		FileOutputStream fos = new FileOutputStream(new File(initConfig.jenkins.jenkinsConfigFileLocation))
		XmlUtil xmlUtil = new XmlUtil()
		xmlUtil.serialize(jenkinsConfig,fos)
		fos.close()
	}
	
	private def changeTargetSystemMappingContent() {
		
		def targetSystemMappingFile = new File(initConfig.targetSystemMappings)
		def targetSystemMappingContent = new JsonSlurper().parse(targetSystemMappingFile)

		updateTargetSystemMapping(targetSystemMappingContent,"Entwicklung")
		updateTargetSystemMapping(targetSystemMappingContent,"Informatiktest")
		updateTargetSystemMapping(targetSystemMappingContent,"Produktion")
		updateTargetSystemMappingOtherInstance(targetSystemMappingContent)
					
		targetSystemMappingFile.delete()
		targetSystemMappingFile.write(new JsonBuilder(targetSystemMappingContent).toPrettyString())
	}
	
	private def updateTargetSystemMappingOtherInstance(def targetSystemMappingContent) {
		targetSystemMappingContent.otherTargetInstances = []
		def newInstancesList = initConfig.target.system.mapping.otherTargetInstances.new
		newInstancesList.split(",").each({instance ->
			targetSystemMappingContent.otherTargetInstances.add(instance)
		})
	}
	
	private def updateTargetSystemMapping(def targetSystemMappingContent, def targetName) {
		targetSystemMappingContent.targetSystems.each({targetSystem ->
			if (targetSystem.name.equals(targetName)) {
				targetSystem.target = getNewTarget(targetName)
			}
		})
	}
	
	private getNewTarget(String targetName) {
		def targetNameLowerCase = targetName.toLowerCase()
		return initConfig.target.system.mapping."${targetNameLowerCase}".new
	}
	
	private def backupFile(def originalFileName) {
		def originalFile = new File(originalFileName)
		def backupFile = new File("${originalFileName}.backup")
		Files.copy(originalFile.toPath(), backupFile.toPath())
		println "Backup created for ${originalFileName} : ${backupFile.getPath()}"
	}

}
