package com.apgsga.patch.service.client.rest

import com.apgsga.microservice.patch.api.*
import com.apgsga.patch.service.client.PatchCliExceptionHandler
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.client.RestTemplate

class PatchRestServiceClient implements PatchOpService {


	private String baseUrl

	private RestTemplate restTemplate


	PatchRestServiceClient(def baseUrl) {
		this.baseUrl = baseUrl
		this.restTemplate = new RestTemplate()
		restTemplate.setErrorHandler(new PatchCliExceptionHandler())
	}


	def getRestBaseUri() {
		"http://" + baseUrl + "/patch/private"
	}

	@Override
	void cleanLocalMavenRepo() {
		restTemplate.postForLocation(getRestBaseUri() + "/cleanLocalMavenRepo", null)
	}

	@Override
	void startAssembleAndDeployPipeline(AssembleAndDeployParameters parameters) {
		restTemplate.postForLocation(getRestBaseUri() + "/startAssembleAndDeployPipeline", parameters)
	}

	@Override
	void startInstallPipeline(InstallParameters parameters) {
		restTemplate.postForLocation(getRestBaseUri() + "/startInstallPipeline", parameters)
	}

	void save(File patchFile, Class<Patch> clx) {
		println "File ${patchFile} to be uploaded"
		ObjectMapper mapper = new ObjectMapper()
		def patchData = mapper.readValue(patchFile, clx)
		save(patchData)
	}

	@Override
	void build(BuildParameter buildParameters) {
		restTemplate.postForLocation(getRestBaseUri() + "/build",buildParameters)
	}

	@Override
	void setup(SetupParameter setupParams) {
		restTemplate.postForLocation(getRestBaseUri() + "/setup", setupParams)
	}

    @SuppressWarnings('GroovyMissingReturnStatement')
    @Override
    Patch save(Patch patch) {
		restTemplate.postForLocation(getRestBaseUri() + "/save", patch)
		println patch.toString() + " Saved Patch."
	}

	@Override
	void savePatchLog(String patchNumber, PatchLogDetails patchLogDetails) {
		restTemplate.postForLocation(getRestBaseUri() + "/savePatchLog/${patchNumber}", patchLogDetails)
		println "Saved PatchLog for " + patchNumber
	}

	@Override
	void notify(NotificationParameters params) {
		restTemplate.postForLocation(getRestBaseUri() + "/notify", params)
		println "DB Notified with following params: " + params.toString()
	}

	@Override
	void startOnDemandPipeline(OnDemandParameter parameters) {
		restTemplate.postForLocation(getRestBaseUri() + "/onDemand", parameters)
	}

	@Override
	void startOnClonePipeline(OnCloneParameters parameters) {
		restTemplate.postForLocation(getRestBaseUri() + "/onClone", parameters)
	}

	@Override
	void checkPatchConflicts(List<PatchListParameter> parameters) {
		restTemplate.postForLocation(getRestBaseUri() + "/checkPatchConflicts", parameters)
	}

}