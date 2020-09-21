package com.apgsga.microservice.patch.api;

import java.io.IOException;
import java.util.List;

public interface PatchPersistence {

	Patch findById(String patchNumber);
	
	PatchLog findPatchLogById(String patchNumber);

	Boolean patchExists(String patchNumber);

	List<String> findAllPatchIds();

	void savePatch(Patch patch);
	
	void savePatchLog(String patchNumber);

	void removePatch(Patch patch);

	void saveDbModules(DbModules dbModules);

	DbModules getDbModules();

	void saveServicesMetaData(ServicesMetaData serviceData);

	ServicesMetaData getServicesMetaData();

	ServiceMetaData findServiceByName(String serviceName);
	
	List<String> listAllFiles();
	
	List<String> listFiles(String prefix);

	void clean();

}
