package com.apgsga.microservice.patch.core.impl;

import java.util.Map;

public interface PatchActionExecutor {
	
	public void execute(String patchNumber, String toStatus, Map<String,PatchAction> action);

}
