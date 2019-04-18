package com.apgsga.microservice.patch.api;

public interface PatchLogService {
	
	PatchLog findLogById(String patchNumber);
	
	void save(PatchLog patchLog);
}
