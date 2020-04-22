package com.apgsga.patch.service.client.serverless

import com.apgsga.microservice.patch.api.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Lists
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate

class PatchServerlessImpl implements PatchOpService, PatchPersistence {

	public PatchServerlessImpl(def config) {
	}

	@Override
	public void restartProdPipeline(String patchNumber) {
	}

	@Override
	public void executeStateTransitionAction(String patchNumber, String toStatus) {
	}
	@Override
	public void cleanLocalMavenRepo() {
	}

	@Override
	public Patch findById(String patchNumber) {
	}
	
	@Override
	public PatchLog findPatchLogById(String patchNummer) {
	}


	@Override
	public Boolean patchExists(String patchNumber) {
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
	}
	
	@Override
	public void savePatchLog(Patch patch) {
	}
	
	@Override
	public void savePatch(Patch patch) {
	}

	@Override
	public List<String> findAllPatchIds() {
	}


	@Override
	public void removePatch(Patch patch) {

	}

	@Override
	public void saveDbModules(DbModules dbModules) {
	}

	@Override
	public DbModules getDbModules() {
	}

	@Override
	public void saveServicesMetaData(ServicesMetaData serviceData) {
	}

	@Override
	public List<String> listAllFiles() {
	}


	@Override
	public List<String> listFiles(String prefix) {
	}


	@Override
	public ServicesMetaData getServicesMetaData() {
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
	public void onClone(String source, String target) {
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