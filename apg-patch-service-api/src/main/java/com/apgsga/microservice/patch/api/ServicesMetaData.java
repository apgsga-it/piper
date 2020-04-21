package com.apgsga.microservice.patch.api;

import com.affichage.persistence.common.client.AbstractTransientEntity;

import java.util.List;

public class ServicesMetaData extends AbstractTransientEntity {
	
	private static final long serialVersionUID = 1L;
	private List<ServiceMetaData> servicesMetaData;

	public List<ServiceMetaData> getServicesMetaData() {
		return servicesMetaData;
	}

	public void setServicesMetaData(List<ServiceMetaData> data) {
		this.servicesMetaData = data;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((servicesMetaData == null) ? 0 : servicesMetaData.hashCode());
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
		ServicesMetaData other = (ServicesMetaData) obj;
		if (servicesMetaData == null) {
			return other.servicesMetaData == null;
		} else return servicesMetaData.equals(other.servicesMetaData);
	}

	@Override
	public String toString() {
		return "ServicesMetaData [servicesMetaData=" + servicesMetaData + "]";
	}
	
	

}
