package com.apgsga.microservice.patch.api;

public interface PatchLogPersistence {

	PatchLog findLogById(String patchNumber);
	
	void save(PatchLog patchLog);	
}
