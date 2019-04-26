package com.apgsga.microservice.patch.api;

import java.io.IOException;
import java.util.List;

public interface PatchPersistence {

	Patch findById(String patchNummer);
	
	PatchLog findPatchLogById(String patchNummer);

	Boolean patchExists(String patchNummber);

	public List<String> findAllPatchIds();

	public void savePatch(Patch patch);
	
	public void savePatchLog(Patch patch);

	public void removePatch(Patch patch);

	public void saveDbModules(DbModules dbModules);

	public DbModules getDbModules();

	public void saveServicesMetaData(ServicesMetaData serviceData);

	public ServicesMetaData getServicesMetaData();

	public ServiceMetaData findServiceByName(String serviceName);
	
	public List<String> listAllFiles();
	
	public List<String> listFiles(String prefix); 

	public void clean();

	public void init() throws IOException;
	
	public List<Patch> findWithObjectName(String objectName);
}
