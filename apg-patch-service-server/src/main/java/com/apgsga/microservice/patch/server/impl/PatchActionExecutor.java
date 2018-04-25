package com.apgsga.microservice.patch.server.impl;

public interface PatchActionExecutor {
	
	public void execute(String patchNumber, String toStatus); 

}
