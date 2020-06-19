package com.apgsga.microservice.patch.api;

import java.io.IOException;
import java.util.List;

public interface PatchPersistence {

	Patch findById(String patchNummer);
	
	PatchLog findPatchLogById(String patchNummer);

	Boolean patchExists(String patchNummber);

	List<String> findAllPatchIds();

	void savePatch(Patch patch);
	
	void savePatchLog(Patch patch);

	void removePatch(Patch patch);

	void saveDbModules(DbModules dbModules);

	DbModules getDbModules();

	void saveServicesMetaData(ServicesMetaData serviceData);

	ServicesMetaData getServicesMetaData();

	ServiceMetaData findServiceByName(String serviceName);
	
	List<String> listAllFiles();
	
	List<String> listFiles(String prefix);

	void clean();

	void init() throws IOException;
}
