package com.apgsga.patch.service.client

import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate

import com.apgsga.microservice.patch.api.DbModules
import com.apgsga.microservice.patch.api.Patch
import com.apgsga.microservice.patch.api.PatchOpService
import com.apgsga.microservice.patch.api.PatchPersistence
import com.apgsga.microservice.patch.api.ServiceMetaData
import com.apgsga.microservice.patch.api.ServicesMetaData
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Lists

class PatchServiceClient implements PatchOpService, PatchPersistence {


	private String baseUrl;

	private RestTemplate restTemplate;


	public PatchServiceClient(def config) {
		this.baseUrl = config.host.default;
		this.restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new PatchCliExceptionHandler())
	}


	def getRestBaseUri() {
		"http://" + baseUrl + "/patch/private";
	}

	@Override
	public void restartProdPipeline(String patchNumber) {
		restTemplate.postForLocation(getRestBaseUri() + "/restartProdPatchPipeline/{patchNumber}", null, [patchNumber:patchNumber]);
	}

	@Override
	public void executeStateTransitionAction(String patchNumber, String toStatus) {
		restTemplate.postForLocation(getRestBaseUri() + "/executeStateChangeAction/{patchNumber}/{toStatus}", null, [patchNumber:patchNumber,toStatus:toStatus]);
	}	@Override
	public void cleanLocalMavenRepo() {
		restTemplate.postForLocation(getRestBaseUri() + "/cleanLocalMavenRepo", null);
	}

	@Override
	public Patch findById(String patchNumber) {
		return restTemplate.getForObject(getRestBaseUri() + "/findById/{id}", Patch.class, [id:patchNumber]);
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


	@Override
	public void onClone(String target) {
		restTemplate.postForLocation(getRestBaseUri() + "/onClone?target=${target}", null)
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
