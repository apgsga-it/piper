package com.apgsga.microservice.patch.api;

import java.util.List;

import com.affichage.persistence.common.client.AbstractTransientEntity;

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
			if (other.servicesMetaData != null)
				return false;
		} else if (!servicesMetaData.equals(other.servicesMetaData))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ServicesMetaData [servicesMetaData=" + servicesMetaData + "]";
	}
	
	

}
