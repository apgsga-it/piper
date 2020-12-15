package com.apgsga.microservice.patch.api;

import java.util.List;

public interface PatchPersistence {

	Patch findById(String patchNumber);
	
	PatchLog findPatchLogById(String patchNumber);

	Boolean patchExists(String patchNumber);

	List<String> findAllPatchIds();

	void savePatch(Patch patch);
	
	void savePatchLog(String patchNumber, PatchLogDetails logDetails);

	void removePatch(Patch patch);

	void saveDbModules(DbModules dbModules);

	DbModules getDbModules();

	void saveServicesMetaData(ServicesMetaData serviceData);

	ServiceMetaData getServiceMetaDataByName(String serviceName);

	ServicesMetaData getServicesMetaData();
	
	List<String> listAllFiles();
	
	List<String> listFiles(String prefix);

	OnDemandTargets onDemandTargets();

	StageMappings stageMappings();

	TargetInstances targetInstances();

	List<Package> packagesFor(Service service);

	String targetFor(String stageName);

	List<String> patchIdsForStatus(String statusCode);

	void notify(NotificationParameters params);

	void clean();

}
