package com.apgsga.microservice.patch.api.impl;

import java.util.Date;

import com.affichage.persistence.common.client.AbstractTransientEntity;
import com.apgsga.microservice.patch.api.PatchLogDetails;

public class PatchLogDetailsBean extends AbstractTransientEntity implements PatchLogDetails{

	private static final long serialVersionUID = 1L;
	private Date datetime;
	private String target;
	private String step;
	
	@Override
	public Date getDateTime() {
		return datetime;
	}

	@Override
	public void setDateTime(Date datetime) {
		final Object oldValue = this.datetime;
		this.datetime = datetime;
		firePropertyChange(DATETIME, oldValue, datetime);
	}

	@Override
	public String getTarget() {
		return target;
	}

	@Override
	public void setTarget(String target) {
		final Object oldValue = this.target;
		this.target = target;
		firePropertyChange(TARGET, oldValue, target);
	}

	@Override
	public String getStep() {
		return step;
	}

	@Override
	public void setStep(String step) {
		final Object oldValue = this.step;
		this.step = step;
		firePropertyChange(STEP, oldValue, step);
	}
	
	@Override
	public String toString() {
		return "PatchLogDetailsBean [datetime=" + datetime + ",target=" + target + ",step=" + step + "]";		
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PatchLogDetailsBean other = (PatchLogDetailsBean) obj;
		if(datetime == null) {
			if(other.getDateTime() != null) {
				return false;
			}
		}
		else if(!datetime.equals(other.getDateTime())) {
			return false;
		}
		if(target == null) {
			if(other.getTarget() != null) {
				return false;
			}
		}
		else if(!target.equals(other.getTarget())) {
			return false;
		}
		if(step == null) {
			if(other.getStep() != null) {
				return false;
			}
		}
		else if(!step.equals(other.getStep())) {
			return false;
		}
		return true;
	}
}
