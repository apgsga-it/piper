package com.apgsga.microservice.patch.api;

import java.util.List;

public class TargetSystemEnvironments {
	
	private List<TargetSystemEnviroment> targetSystemEnviroments;

	public TargetSystemEnvironments() {
		super();
	}

	public TargetSystemEnvironments(List<TargetSystemEnviroment> targetSystemEnviroments) {
		super();
		this.targetSystemEnviroments = targetSystemEnviroments;
	}

	public List<TargetSystemEnviroment> getTargetSystemEnviroments() {
		return targetSystemEnviroments;
	}

	public void setTargetSystemEnviroments(List<TargetSystemEnviroment> targetSystemEnviroments) {
		this.targetSystemEnviroments = targetSystemEnviroments;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((targetSystemEnviroments == null) ? 0 : targetSystemEnviroments.hashCode());
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
		TargetSystemEnvironments other = (TargetSystemEnvironments) obj;
		if (targetSystemEnviroments == null) {
			if (other.targetSystemEnviroments != null)
				return false;
		} else if (!targetSystemEnviroments.equals(other.targetSystemEnviroments))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TargetSystemEnviroments [targetSystemEnviroments=" + targetSystemEnviroments + "]";
	}
	
	
	

}
