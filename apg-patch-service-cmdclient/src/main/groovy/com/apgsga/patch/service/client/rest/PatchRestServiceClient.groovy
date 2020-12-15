package com.apgsga.patch.service.client.rest

import com.apgsga.microservice.patch.api.*
import com.apgsga.patch.service.client.PatchCliExceptionHandler
import com.fasterxml.jackson.databind.ObjectMapper
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
	void build(BuildParameter buildParameters) {
		restTemplate.postForLocation(getRestBaseUri() + "/build",buildParameters)
	}

	@Override
	void setup(SetupParameter setupParams) {
		restTemplate.postForLocation(getRestBaseUri() + "/setup", setupParams)
	}

	@Override
	public Patch save(Patch patch) {
		restTemplate.postForLocation(getRestBaseUri() + "/save", patch);
		println patch.toString() + " Saved Patch."
	}

	@Override
	void savePatchLog(String patchNumber, PatchLogDetails patchLogDetails) {
		restTemplate.postForLocation(getRestBaseUri() + "/savePatchLog/${patchNumber}", patchLogDetails);
		println "Saved PatchLog for " + patchNumber;
	}

	@Override
	void notify(NotificationParameters params) {
		restTemplate.postForLocation(getRestBaseUri() + "/notify", params)
		println "DB Notified with following params: " + params.toString()
	}

	@Override
	List<String> patchIdsForStatus(String statusCode) {
		return restTemplate.getForObject(getRestBaseUri() + "/patchIdsForStatus/{status}", String[].class, [status:statusCode]);
	}

}