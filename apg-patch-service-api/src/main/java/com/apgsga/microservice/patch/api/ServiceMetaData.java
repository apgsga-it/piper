package com.apgsga.microservice.patch.api;


import com.affichage.persistence.common.client.AbstractTransientEntity;

public class ServiceMetaData extends AbstractTransientEntity {

	private static final long serialVersionUID = 1L;
	private String serviceName; 
	private String microServiceBranch;
	private String baseVersionNumber;
	private String revisionMnemoPart;
	private String packagerName;

	public ServiceMetaData(String serviceName, String microServiceBranch, String baseVersionNumber,
						   String revisionMnemoPart, String packagerName) {
		super();
		this.serviceName = serviceName;
		this.microServiceBranch = microServiceBranch;
		this.baseVersionNumber = baseVersionNumber;
		this.revisionMnemoPart = revisionMnemoPart;
		this.packagerName = packagerName;
	}

	public ServiceMetaData() {
	}

	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getMicroServiceBranch() {
		return microServiceBranch;
	}
	public void setMicroServiceBranch(String microServiceBranch) {
		this.microServiceBranch = microServiceBranch;
	}
	public String getBaseVersionNumber() {
		return baseVersionNumber;
	}
	public void setBaseVersionNumber(String baseVersionNumber) {
		this.baseVersionNumber = baseVersionNumber;
	}
	public String getRevisionMnemoPart() {
		return revisionMnemoPart;
	}
	public void setRevisionMnemoPart(String revisionMnemoPart) {
		this.revisionMnemoPart = revisionMnemoPart;
	}
	public String getPackagerName(){ return packagerName; }
	public void setPackagerName(String packagerName) { this.packagerName = packagerName; }
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((baseVersionNumber == null) ? 0 : baseVersionNumber.hashCode());
		result = prime * result + ((microServiceBranch == null) ? 0 : microServiceBranch.hashCode());
		result = prime * result + ((revisionMnemoPart == null) ? 0 : revisionMnemoPart.hashCode());
		result = prime * result + ((serviceName == null) ? 0 : serviceName.hashCode());
		result = prime * result + ((packagerName == null) ? 0 : packagerName.hashCode());
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
		ServiceMetaData other = (ServiceMetaData) obj;
		if (baseVersionNumber == null) {
			if (other.baseVersionNumber != null)
				return false;
		} else if (!baseVersionNumber.equals(other.baseVersionNumber))
			return false;
		if (microServiceBranch == null) {
			if (other.microServiceBranch != null)
				return false;
		} else if (!microServiceBranch.equals(other.microServiceBranch))
			return false;
		if (revisionMnemoPart == null) {
			if (other.revisionMnemoPart != null)
				return false;
		} else if (!revisionMnemoPart.equals(other.revisionMnemoPart))
			return false;
		if (serviceName == null) {
			return other.serviceName == null;
		} else return serviceName.equals(other.serviceName);
	}
	@Override
	public String toString() {
		return "PatchLogDetails [serviceName=" + serviceName + ", microServiceBranch=" + microServiceBranch
				+ ", baseVersionNumber=" + baseVersionNumber + ", revisionMnemoPart=" + revisionMnemoPart
				+ ", packagerName=" + packagerName + "]";
	} 
}
