package com.apgsga.microservice.patch.api;

import com.affichage.persistence.common.client.EntityRootInterface;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@EntityRootInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
public interface DbObject  {

	public static final String MODULE_NAME = "moduleName";
	public static final String FILE_NAME = "fileName";
	public static final String FILE_PATH = "filePath";
	
	String getModuleName();

	void setModuleName(String moduleName);	

	String getFileName();

	void setFileName(String fileName);

	String getFilePath();

	void setFilePath(String filePath);
	
	String asFullPath();
	
	public boolean hasConflict();
	
	public void setHasConflict(boolean hasConflict);

}
