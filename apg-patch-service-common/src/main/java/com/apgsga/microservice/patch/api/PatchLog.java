package com.apgsga.microservice.patch.api;

import java.util.Date;
import java.util.List;
import java.util.function.LongToDoubleFunction;

import com.affichage.persistence.common.client.EntityRootInterface;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@EntityRootInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
public interface PatchLog {

	public static final String PATCH_NUMBER = "patchNumber";
	
	public static final String LOG_DETAILS = "logDetails";
	
	String getPatchNumber();
	
	void setPatchNumber(String patchNumber);
	
	List<PatchLogDetails> getLogDetails();
	
	void addLog(PatchLogDetails logDetails);
	
}
