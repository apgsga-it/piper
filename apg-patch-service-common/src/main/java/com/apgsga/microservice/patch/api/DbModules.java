package com.apgsga.microservice.patch.api;

import java.util.List;

import com.google.common.collect.Lists;

public class DbModules {
	
	private List<String> dbModules = Lists.newArrayList();

	public DbModules(List<String> dbModules) {
		super();
		this.dbModules = dbModules;
	}

	public DbModules() {
		super();
	}

	public List<String> getDbModules() {
		return dbModules;
	}

	public void setDbModules(List<String> dbModules) {
		this.dbModules = dbModules;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dbModules == null) ? 0 : dbModules.hashCode());
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
		DbModules other = (DbModules) obj;
		if (dbModules == null) {
			if (other.dbModules != null)
				return false;
		} else if (!dbModules.equals(other.dbModules))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DbModules [dbModules=" + dbModules + "]";
	}

}
