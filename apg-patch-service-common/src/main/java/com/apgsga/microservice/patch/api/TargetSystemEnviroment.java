package com.apgsga.microservice.patch.api;

import com.affichage.persistence.common.client.EntityRootInterface;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@EntityRootInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
public interface TargetSystemEnviroment {
	public String getName();
	public void setName(String targetSystemName);
	public String getTargetTypIndicator();
	public void setTargetTypIndicator(String targetTypIndicator);

}
