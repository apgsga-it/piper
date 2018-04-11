package com.apgsga.microservice.patch.api;

import java.util.List;

import com.affichage.persistence.common.client.EntityRootInterface;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@EntityRootInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
public interface ServicesMetaData {
	
	public List<ServiceMetaData> getServicesMetaData();
	public void setServicesMetaData(List<ServiceMetaData> data); 

}
