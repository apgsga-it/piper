package com.apgsga.microservice.patch.core.impl;

import java.util.Map;

public class MockPatchAction implements PatchAction {


	@Override
	public String executeToStateAction(String patchNumber, String toAction,  Map<String,String> parameter) {
		return "Mock : " + patchNumber + " , toAction: " + toAction + " with parameter: " + parameter.toString(); 
		
	}

}
