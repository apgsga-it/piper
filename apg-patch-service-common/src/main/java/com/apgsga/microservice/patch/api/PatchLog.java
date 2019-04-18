package com.apgsga.microservice.patch.api;

import java.util.Date;

import com.affichage.persistence.common.client.EntityRootInterface;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@EntityRootInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
public interface PatchLog {

	// TODO JHE: not sure if needed/correct
	public static final String PATCH_NUMBER = "patchNumber";
	
	public static final String DATETIME = "datetime";
	
	public static final String TARGET = "target";
	
	public static final String STEP = "step";
	
	// TODO JHE: not sure if needed/correct
	String getPatchNumber();
	
	// TODO JHE: not sure if needed/correct
	void setPatchNumber(String patchNumber);
	
	String getDateTime();
	
	void setDateTime(Date datetime);
	
	String getTarget();
	
	void setTarget(String target);
	
	String getStep();
	
	void setStep(String step);
}
