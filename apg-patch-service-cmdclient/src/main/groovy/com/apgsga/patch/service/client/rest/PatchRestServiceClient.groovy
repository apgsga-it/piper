package com.apgsga.patch.service.client.rest

import com.apgsga.microservice.patch.api.Patch
import com.apgsga.microservice.patch.api.PatchOpService
import com.apgsga.patch.service.client.PatchCliExceptionHandler
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate

class PatchRestServiceClient implements PatchOpService {


	private String baseUrl;

	private RestTemplate restTemplate;


	public PatchRestServiceClient(def baseUrl) {
		this.baseUrl = baseUrl;
		this.restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new PatchCliExceptionHandler())
	}


	def getRestBaseUri() {
		"http://" + baseUrl + "/patch/private";
	}

	@Override
	public void executeStateTransitionAction(String patchNumber, String toStatus) {
		restTemplate.postForLocation(getRestBaseUri() + "/executeStateChangeAction/{patchNumber}/{toStatus}", null, [patchNumber:patchNumber,toStatus:toStatus]);
	}
	@Override
	public void cleanLocalMavenRepo() {
		restTemplate.postForLocation(getRestBaseUri() + "/cleanLocalMavenRepo", null);
	}

	@Override
	void startAssembleAndDeployPipeline(String target) {
		restTemplate.postForLocation(getRestBaseUri() + "/startAssembleAndDeployPipeline", target)
	}

	@Override
	void startInstallPipeline(String target) {
		restTemplate.postForLocation(getRestBaseUri() + "/startInstallPipeline", target)
	}

	@Override
	void copyPatchFiles(Map params) {
		restTemplate.postForLocation(getRestBaseUri() + "/copyPatchFiles", params);
	}


	public void save(File patchFile, Class<Patch> clx) {
		println "File ${patchFile} to be uploaded"
		ObjectMapper mapper = new ObjectMapper();
		def patchData = mapper.readValue(patchFile, clx)
		save(patchData)
	}

	@Override
	public Patch save(Patch patch) {
		restTemplate.postForLocation(getRestBaseUri() + "/save", patch);
		println patch.toString() + " Saved Patch."
	}

	@Override
	void savePatchLog(String patchNumber) {
		restTemplate.postForLocation(getRestBaseUri() + "/savePatchLog", patchNumber)
		println "Saved PatchLog for " + patchNumber
	}

	@Override
	void executeStateTransitionActionInDb(String patchNumber, Long statusNum) {
		restTemplate.postForLocation(getRestBaseUri() + "/executeStateTransitionActionInDb/{patchNumber}/{statusNum}", null, [patchNumber:patchNumber,statusNum:statusNum])
	}

	@Override
	List<String> patchIdsForStatus(String statusCode) {
		return restTemplate.getForObject(getRestBaseUri() + "/patchIdsForStatus/{status}", String[].class, [status:statusCode]);
	}


	class PatchServiceErrorHandler implements ResponseErrorHandler {



		public PatchServiceErrorHandler() {
		}

		@Override
		public boolean hasError(ClientHttpResponse response) throws IOException {

			return false;
		}

		@Override
		public void handleError(ClientHttpResponse response) throws IOException {
			System.err.println "Recieved Error from Server with Http Code: ${response.getStatusText()}"
			System.err.println "Error output : " + response.body.getText("UTF-8")
		}
	}
}