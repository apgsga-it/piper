package com.apgsga.patch.service.client.rest


import com.apgsga.microservice.patch.api.Patch
import com.apgsga.microservice.patch.api.PatchOpService
import com.apgsga.patch.service.client.PatchCliExceptionHandler
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.client.RestTemplate

class PatchRestServiceClient implements PatchOpService {


	private String baseUrl

	private RestTemplate restTemplate


	PatchRestServiceClient(def config) {
		this.baseUrl = config.host.default
		this.restTemplate = new RestTemplate()
		restTemplate.setErrorHandler(new PatchCliExceptionHandler())
	}


	def getRestBaseUri() {
		"http://" + baseUrl + "/patch/private"
	}

	@Override
	void executeStateTransitionAction(String patchNumber, String toStatus) {
		restTemplate.postForLocation(getRestBaseUri() + "/executeStateChangeAction/{patchNumber}/{toStatus}", null, [patchNumber:patchNumber,toStatus:toStatus])
	}
	@Override
	void cleanLocalMavenRepo() {
		restTemplate.postForLocation(getRestBaseUri() + "/cleanLocalMavenRepo", null)
	}

	@Override
	Patch save(Patch patch) {
		restTemplate.postForLocation(getRestBaseUri() + "/save", patch)
		println patch.toString() + " Saved Patch."
	}



	@Override
	void onClone(String source, String target) {
		restTemplate.postForLocation(getRestBaseUri() + "/onClone?source=${source}&target=${target}", null)
	}


}