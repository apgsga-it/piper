package com.apgsga.microservice.patch.api.impl;

import java.util.Date;

import com.affichage.persistence.common.client.AbstractTransientEntity;
import com.apgsga.microservice.patch.api.PatchLogDetails;

public class PatchLogDetailsBean extends AbstractTransientEntity implements PatchLogDetails{

	private static final long serialVersionUID = 1L;
	private Date datetime;
	private String target;
	private String patchPipelineTask;
	private String logText;
	
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
	public String getPatchPipelineTask() {
		return patchPipelineTask;
	}

	@Override
	public void setPatchPipelineTask(String patchPipelineTask) {
		final Object oldValue = this.patchPipelineTask;
		this.patchPipelineTask = patchPipelineTask;
		firePropertyChange(PATCH_PIPELINE_TASK, oldValue, patchPipelineTask);
	}

	@Override
	public String getLogText() {
		return logText;
	}

	@Override
	public void setLogText(String logText) {
		final Object oldValue = this.logText;
		this.logText = logText;
		firePropertyChange(LOG_TEXT, oldValue, logText);
	}
	
	@Override
	public String toString() {
		return "PatchLogDetailsBean [datetime=" + datetime + 
									 ",target=" + target + 
									 ",patchPipelineTask=" + patchPipelineTask + 
									 ",logText=" + logText + "]";		
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
		if(patchPipelineTask == null) {
			if(other.getPatchPipelineTask() != null) {
				return false;
			}
		}
		else if(!patchPipelineTask.equals(other.getPatchPipelineTask())) {
			return false;
		}
		if(logText == null) {
			if(other.getLogText() != null) {
				return false;
			}
		}
		else if(!logText.equals(other.getLogText())) {
			return false;
		}
		return true;
	}
}
