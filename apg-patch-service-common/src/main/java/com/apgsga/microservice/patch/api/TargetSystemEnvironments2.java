package com.apgsga.microservice.patch.api;

import java.util.Map;
import java.util.Set;

public class TargetSystemEnvironments2 {

	private Set<String> otherInstances;
	private Map<String, Integer> stateMap;
	private Map<String,String> logicalNameInstanceMap;

	private TargetSystemEnvironments2() {
		super();
	}

	public Set<String> getOtherInstances() {
		return otherInstances;
	}

	public void setOtherInstances(Set<String> otherInstances) {
		this.otherInstances = otherInstances;
	}

	public Map<String, Integer> getStateMap() {
		return stateMap;
	}

	public void setStateMap(Map<String, Integer> stateMap) {
		this.stateMap = stateMap;
	}

	public Map<String, String> getLogicalNameInstanceMap() {
		return logicalNameInstanceMap;
	}

	public void setLogicalNameInstanceMap(Map<String, String> logicalNameInstanceMap) {
		this.logicalNameInstanceMap = logicalNameInstanceMap;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((logicalNameInstanceMap == null) ? 0 : logicalNameInstanceMap.hashCode());
		result = prime * result + ((otherInstances == null) ? 0 : otherInstances.hashCode());
		result = prime * result + ((stateMap == null) ? 0 : stateMap.hashCode());
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
		TargetSystemEnvironments2 other = (TargetSystemEnvironments2) obj;
		if (logicalNameInstanceMap == null) {
			if (other.logicalNameInstanceMap != null)
				return false;
		} else if (!logicalNameInstanceMap.equals(other.logicalNameInstanceMap))
			return false;
		if (otherInstances == null) {
			if (other.otherInstances != null)
				return false;
		} else if (!otherInstances.equals(other.otherInstances))
			return false;
		if (stateMap == null) {
			if (other.stateMap != null)
				return false;
		} else if (!stateMap.equals(other.stateMap))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TargetSystemEnvironments2 [otherInstances=" + otherInstances + ", stateMap=" + stateMap
				+ ", nameInstanceMap=" + logicalNameInstanceMap + "]";
	}

	


}
