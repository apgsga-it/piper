package com.apgsga.microservice.patch.api;

import com.affichage.persistence.common.client.AbstractTransientEntity;
import com.google.common.collect.Lists;

import java.util.List;

public class PatchLog extends AbstractTransientEntity  {

	private static final long serialVersionUID = 1L;
	public static final String PATCH_NUMBER = "patchNumber";

	public static final String LOG_DETAILS = "logDetails";
	private String patchNumber;
	private List<PatchLogDetails> logDetails;
	
	public PatchLog() {
		this.logDetails = Lists.newArrayList();
	}

	public String getPatchNumber() {
		return patchNumber;
	}

	public void setPatchNumber(String patchNumber) {
		final Object oldValue = this.patchNumber;
		this.patchNumber = patchNumber;
		firePropertyChangeEvent(PATCH_NUMBER, oldValue, patchNumber);
	}

	public List<PatchLogDetails> getLogDetails() {
		return logDetails;
	}

	public void addLog(PatchLogDetails logDetails) {
		this.logDetails.add(logDetails);
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder("PatchLog [patchNumber=" + patchNumber + ", logDetails = {");
		for(PatchLogDetails ld : logDetails) {
			s.append(ld.toString()).append(",");
		}		
		s = new StringBuilder(s.substring(0, s.length() - 1)); // Remove last comma
		s.append("}]");
		return s.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		PatchLog other = (PatchLog) obj;
		if(patchNumber == null) {
			if(other.getPatchNumber() != null) {
				return false;
			}
		}
		else if(!patchNumber.equals(other.getPatchNumber())) {
			return false;
		}
		
		if(logDetails.isEmpty()) {
			return other.getLogDetails().isEmpty();
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