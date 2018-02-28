package com.apgsga.patch.service.client

import java.util.List
import java.util.Map

import org.springframework.web.client.RestTemplate

import com.apgsga.microservice.patch.api.DbModules
import com.apgsga.microservice.patch.api.Patch
import com.apgsga.microservice.patch.api.PatchOpService
import com.apgsga.microservice.patch.api.PatchPersistence
import com.apgsga.microservice.patch.api.ServiceMetaData
import com.apgsga.microservice.patch.api.ServicesMetaData
import com.apgsga.microservice.patch.api.TargetSystemEnviroment
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Lists
import com.google.common.collect.Maps

class PatchServiceClient implements PatchOpService, PatchPersistence {


	private String baseUrl;

	private RestTemplate restTemplate;


	public PatchServiceClient(String url) {
		this.baseUrl = url;
		this.restTemplate = new RestTemplate();
	}


	def getRestBaseUri() {
		"http://" + baseUrl + "patchdb";
	}
	
	@Override
	public void executeStateTransitionAction(String patchNumber, String toStatus) {
		restTemplate.postForLocation(getRestBaseUri() + "/executeStateChangeAction/{patchNumber}/{toStatus}", null, [patchNumber:patchNumber,toStatus:toStatus]);
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
	public void saveTargetSystemEnviroments(List<TargetSystemEnviroment> installationTargets) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<TargetSystemEnviroment> getInstallationTargets() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TargetSystemEnviroment getInstallationTarget(String installationTarget) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceMetaData findServiceByName(String serviceName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clean() {
		

	}

	@Override
	public void init() throws IOException {
		// TODO Auto-generated method stub

	}


}
