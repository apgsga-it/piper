package com.apgsga.patch.service.client

import java.util.List
import java.util.Map

import org.springframework.web.client.RestTemplate

import com.apgsga.microservice.patch.api.DbModules
import com.apgsga.microservice.patch.api.MavenArtifact
import com.apgsga.microservice.patch.api.Patch
import com.apgsga.microservice.patch.api.PatchOpService
import com.apgsga.microservice.patch.api.PatchPersistence
import com.apgsga.microservice.patch.api.ServiceMetaData
import com.apgsga.microservice.patch.api.ServicesMetaData
import com.apgsga.microservice.patch.api.TargetSystemEnviroment
import com.apgsga.microservice.patch.api.TargetSystemEnvironments
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Lists

class PatchServiceClient implements PatchOpService, PatchPersistence {


	private String baseUrl;

	private RestTemplate restTemplate;


	public PatchServiceClient(String url) {
		this.baseUrl = url;
		this.restTemplate = new RestTemplate();
	}


	def getRestBaseUri() {
		"http://" + baseUrl + "/patch/private";
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
	public void saveTargetSystemEnviroments(TargetSystemEnvironments targets) {
		restTemplate.postForLocation(getRestBaseUri() + "/saveTargetSystemEnviroments", targets);
	}


	@Override
	public TargetSystemEnvironments getTargetSystemEnviroments() {
		return restTemplate.getForObject(getRestBaseUri() + "/getTargetSystemEnviroments",
				TargetSystemEnvironments.class);
	}


	@Override
	public void saveTargetSystemEnviroments(List<TargetSystemEnviroment> installationTargets) {
		throw new UnsupportedOperationException("Not needed, see saveTargetSystemEnviroments");
	}

	@Override
	public List<TargetSystemEnviroment> getInstallationTargets() {
		throw new UnsupportedOperationException("Not needed, see getTargetSystemEnviroments");
	}

	@Override
	public TargetSystemEnviroment getInstallationTarget(String installationTarget) {
		throw new UnsupportedOperationException("Not needed, see getTargetSystemEnviroments");
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
	public List<MavenArtifact> invalidArtifactNames(String version,String cvsBranch) {
		def invalidArtifacts = restTemplate.getForObject(getRestBaseUri() + "/validateArtifactNamesFromVersion?version=${version}&cvsbranch=${cvsBranch}", List.class)
		return invalidArtifacts;
	}
}
