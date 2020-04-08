package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.io.FilenameUtils;

import com.affichage.persistence.common.client.AbstractTransientEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "className")
public class DbObject extends AbstractTransientEntity  {

	public static final String MODULE_NAME = "moduleName";
	public static final String FILE_NAME = "fileName";
	public static final String FILE_PATH = "filePath";

	private static final long serialVersionUID = 1L;
	private String fileName;
	private String filePath;
	private String moduleName;
	@JsonIgnore
	private transient boolean hasConflict = false;

	public DbObject() {
		super();
	}

	public DbObject(String fileName, String filePath) {
		super();
		this.fileName = fileName;
		this.filePath = filePath;
	}


	public void setFileName(String fileName) {

		final Object oldValue = this.fileName;
		this.fileName = fileName;
		firePropertyChangeAndMarkDirty(FILE_NAME, oldValue, fileName);
	}

	public String getFileName() {
		return fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		final Object oldValue = this.filePath;
		this.filePath = filePath;
		firePropertyChangeAndMarkDirty(FILE_PATH, oldValue, filePath);
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		final Object oldValue = this.moduleName;
		this.moduleName = moduleName;
		firePropertyChangeAndMarkDirty(MODULE_NAME, oldValue, moduleName);
	}


	public String asFullPath() {
		String fullPath = getFilePath() + "/" + getFileName();
		return FilenameUtils.separatorsToUnix(fullPath);
	}


	public boolean hasConflict() {
		return this.hasConflict;
	}

	public void setHasConflict(boolean hasConflict) {
		this.hasConflict = hasConflict;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
		result = prime * result + ((moduleName == null) ? 0 : moduleName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DbObject other = (DbObject) obj;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (filePath == null) {
			if (other.filePath != null)
				return false;
		} else if (!filePath.equals(other.filePath))
			return false;
		if (moduleName == null) {
			if (other.moduleName != null)
				return false;
		} else if (!moduleName.equals(other.moduleName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DbObjectImpl [fileName=" + fileName + ", filePath=" + filePath + ", moduleName=" + moduleName + "]";
	}



}
