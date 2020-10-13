package com.apgsga.microservice.patch.core.impl;

public interface PatchActionExecutor {
	
	public void execute(String patchNumber, String toStatus);

}
