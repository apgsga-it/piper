package com.apgsga.patch.service.client.serverless

import com.apgsga.microservice.patch.api.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.ResponseErrorHandler

@Component
class PatchServerlessImpl implements PatchOpService, PatchPersistence {

	@Autowired
	@Qualifier("ServerBean")
	private PatchService patchService

	@Autowired
	@Qualifier("ServerBean")
	private PatchOpService patchOpService

	@Autowired
	@Qualifier("patchPersistence")
	private PatchPersistence repo

	PatchServerlessImpl() {
	}

	@Override
	void restartProdPipeline(String patchNumber) {
		patchOpService.restartProdPipeline(patchNumber)
	}

	@Override
	void executeStateTransitionAction(String patchNumber, String toStatus) {
		patchOpService.executeStateTransitionAction(patchNumber,toStatus)
	}
	@Override
	void cleanLocalMavenRepo() {
		patchOpService.cleanLocalMavenRepo()
	}

	@Override
	Patch findById(String patchNumber) {
		patchService.findById(patchNumber)
	}
	
	@Override
	PatchLog findPatchLogById(String patchNummer) {
		patchService.findPatchLogById(patchNumber)
	}


	@Override
	Boolean patchExists(String patchNumber) {
		repo.patchExists(patchNumber)
	}


	void savePatch(File patchFile, Class<Patch> clx) {
		ObjectMapper mapper = new ObjectMapper()
		def patchData = mapper.readValue(patchFile, clx)
		savePatch(patchData)
	}


	void save(File patchFile, Class<Patch> clx) {
		println "File ${patchFile} to be uploaded"
		ObjectMapper mapper = new ObjectMapper()
		def patchData = mapper.readValue(patchFile, clx)
		save(patchData)
	}

	@Override
	Patch save(Patch patch) {
		patchService.save(patch)
	}
	
	@Override
	void savePatchLog(Patch patch) {
		patchService.log(patch)
	}
	
	@Override
	void savePatch(Patch patch) {
		patchService.save(patch)
	}

	@Override
	List<String> findAllPatchIds() {
		repo.findAllPatchIds()
	}


	@Override
	void removePatch(Patch patch) {
		repo.removePatch(patch)
	}

	@Override
	void saveDbModules(DbModules dbModules) {
	    repo.saveDbModules(dbModules)
	}

	@Override
	DbModules getDbModules() {
		repo.getDbModules()
	}

	@Override
	void saveServicesMetaData(ServicesMetaData serviceData) {
		repo.saveServicesMetaData(serviceData)
	}

	@Override
	List<String> listAllFiles() {
		repo.listAllFiles()
	}


	@Override
	List<String> listFiles(String prefix) {
	 	repo.listFiles(prefix)
	}


	@Override
	ServicesMetaData getServicesMetaData() {
		repo.getServicesMetaData()
	}


	@Override
	ServiceMetaData findServiceByName(String serviceName) {
		throw new UnsupportedOperationException("Not needed, see getServiceMetaData")
	}

	@Override
	void clean() {
		throw new UnsupportedOperationException("Cleaning not supported by client")
	}

	@Override
	void init() throws IOException {
		throw new UnsupportedOperationException("Init not supported by client")
	}


	@Override
	void onClone(String source, String target) {
		patchOpService.onClone(source,target)
	}

}