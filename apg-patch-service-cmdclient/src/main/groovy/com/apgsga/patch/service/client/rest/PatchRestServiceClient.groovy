package com.apgsga.patch.service.client.rest

import com.apgsga.microservice.patch.api.*
import com.apgsga.patch.service.client.PatchCliExceptionHandler
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Lists
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate

class PatchRestServiceClient implements PatchOpService, PatchPersistence {


	private String baseUrl;

	private RestTemplate restTemplate;


	public PatchRestServiceClient(def config) {
		this.baseUrl = config.host.default;
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
	public Patch findById(String patchNumber) {
		return restTemplate.getForObject(getRestBaseUri() + "/findById/{id}", Patch.class, [id:patchNumber]);
	}
	
	@Override
	public PatchLog findPatchLogById(String patchNummer) {
		return restTemplate.getForObject(getRestBaseUri() + "/findPatchLogById/{id}", PatchLog.class, [id:patchNummer]);
	}


	@Override
	public Boolean patchExists(String patchNumber) {
		return restTemplate.getForObject(getRestBaseUri() + "/patchExists/{id}", Boolean.class, [id:patchNumber]);
	}


	public void savePatch(File patchFile, Class<Patch> clx) {
		println "File ${patchFile} to be uploaded"
		ObjectMapper mapper = new ObjectMapper();
		def patchData = mapper.readValue(patchFile, clx)
		savePatch(patchData)
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
	public void savePatchLog(Patch patch) {
		restTemplate.postForLocation(getRestBaseUri() + "/savePatchLog", patch)
		println "Saved PatchLog for " + patch.toString()		
	}
	
	@Override
	public void savePatch(Patch patch) {
		restTemplate.postForLocation(getRestBaseUri() + "/savePatch", patch);
		println patch.toString() + " uploaded."
	}

	@Override
	public List<String> findAllPatchIds() {
		String[] result = restTemplate.getForObject(getRestBaseUri() + "/findAllPatchIds", String[].class);
		return Lists.newArrayList(result);
	}


	@Override
	public void removePatch(Patch patch) {
		restTemplate.postForLocation(getRestBaseUri() + "/removePatch", patch);
	}

	@Override
	public void saveDbModules(DbModules dbModules) {
		restTemplate.postForLocation(getRestBaseUri() + "/saveDbModules", dbModules);
	}

	@Override
	public DbModules getDbModules() {
		return restTemplate.getForObject(getRestBaseUri() + "/getDbModules", DbModules.class);
	}

	@Override
	public void saveServicesMetaData(ServicesMetaData serviceData) {
		restTemplate.postForLocation(getRestBaseUri() + "/saveServicesMetaData", serviceData);
	}

	@Override
	public List<String> listAllFiles() {
		return restTemplate.getForObject(getRestBaseUri() + "/listAllFiles",  String[].class);
	}


	@Override
	public List<String> listFiles(String prefix) {
		return restTemplate.getForObject(getRestBaseUri() + "/listFiles/{prefix}", String[].class, [prefix:prefix]);
	}


	@Override
	public ServicesMetaData getServicesMetaData() {
		return restTemplate.getForObject(getRestBaseUri() + "/getServicesMetaData",
				ServicesMetaData.class);
	}


	@Override
	public ServiceMetaData findServiceByName(String serviceName) {
		throw new UnsupportedOperationException("Not needed, see getServiceMetaData");
	}

	@Override
	public void clean() {
		throw new UnsupportedOperationException("Cleaning not supported by client");
	}

	@Override
	public void init() throws IOException {
		throw new UnsupportedOperationException("Init not supported by client");
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