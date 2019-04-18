package com.apgsga.microservice.patch.api.impl;

import java.util.List;

import com.affichage.persistence.common.client.AbstractTransientEntity;
import com.apgsga.microservice.patch.api.PatchLog;
import com.apgsga.microservice.patch.api.PatchLogDetails;
import com.google.common.collect.Lists;

public class PatchLogBean extends AbstractTransientEntity implements PatchLog {

	private static final long serialVersionUID = 1L;
	private String patchNumber;
	private List<PatchLogDetails> logDetails;
	
	public PatchLogBean() {
		this.logDetails = Lists.newArrayList();
	}
	
	@Override
	public String getPatchNumber() {
		return patchNumber;
	}

	@Override
	public void setPatchNumber(String patchNumber) {
		final Object oldValue = this.patchNumber;
		this.patchNumber = patchNumber;
		firePropertyChangeEvent(PATCH_NUMBER, oldValue, patchNumber);
	}

	@Override
	public List<PatchLogDetails> getLogDetails() {
		return logDetails;
	}

	@Override
	public void addLog(PatchLogDetails logDetails) {
		this.logDetails.add(logDetails);
	}
	
	@Override
	public String toString() {
		String s = "PatchLogBean [patchNumber=" + patchNumber + ", logDetails = {";
		for(PatchLogDetails ld : logDetails) {
			s += ld.toString() + ",";
		}		
		s = s.substring(0, s.length()-1); // Remove last comma
		s += "}]";
		return s;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		PatchLogBean other = (PatchLogBean) obj;
		if(patchNumber == null) {
			if(other.getPatchNumber() != null) {
				return false;
			}
		}
		else if(!patchNumber.equals(other.getPatchNumber())) {
			return false;
		}
		
		if(logDetails.isEmpty()) {
			if(!other.getLogDetails().isEmpty()) {
				return false;
			}
		}
		else {
			for(PatchLogDetails ld : logDetails) {
				if(!other.getLogDetails().contains(ld)) {
					return false;
				}
			}
		}
		return true;
	}
}