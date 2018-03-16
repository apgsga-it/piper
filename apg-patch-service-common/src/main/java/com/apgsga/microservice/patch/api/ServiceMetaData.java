package com.apgsga.microservice.patch.api;

import com.affichage.persistence.common.client.EntityRootInterface;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@EntityRootInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
public interface ServiceMetaData {
	
	String getServiceName();

	void setServiceName(String serviceName);

	String getMicroServiceBranch();

	void setMicroServiceBranch(String microServiceBranch);
	
	public String getBaseVersionNumber(); 
	
	public void setBaseVersionNumber(String baseVersionNumber); 
	
	public String getRevisionMnemoPart(); 
	
	public void setRevisionMnemoPart(String revisionMnemoPart); 
}
