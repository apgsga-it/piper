package com.apgsga.microservice.patch.server.impl;

import java.util.Map;

public interface PatchAction {
	
	public String executeToStateAction(String patchNumber, String toAction, Map<String,String> parameter); 

}
