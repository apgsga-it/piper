package com.apgsga.microservice.patch.api;

import java.util.Date;

import com.affichage.persistence.common.client.AbstractTransientEntity;

public class PatchLogDetails extends AbstractTransientEntity {

	private static final long serialVersionUID = 1L;
	public static final String DATETIME = "dateTime";

	public static final String TARGET = "target";

	public static final String PATCH_PIPELINE_TASK = "patchPipelineTask";

	public static final String LOG_TEXT = "logText";
	private Date datetime;
	private String target;
	private String patchPipelineTask;
	private String logText;
	

	public Date getDateTime() {
		return datetime;
	}

	public void setDateTime(Date datetime) {
		final Object oldValue = this.datetime;
		this.datetime = datetime;
		firePropertyChange(DATETIME, oldValue, datetime);
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		final Object oldValue = this.target;
		this.target = target;
		firePropertyChange(TARGET, oldValue, target);
	}

	public String getPatchPipelineTask() {
		return patchPipelineTask;
	}

	public void setPatchPipelineTask(String patchPipelineTask) {
		final Object oldValue = this.patchPipelineTask;
		this.patchPipelineTask = patchPipelineTask;
		firePropertyChange(PATCH_PIPELINE_TASK, oldValue, patchPipelineTask);
	}

	public String getLogText() {
		return logText;
	}

	public void setLogText(String logText) {
		final Object oldValue = this.logText;
		this.logText = logText;
		firePropertyChange(LOG_TEXT, oldValue, logText);
	}
	
	@Override
	public String toString() {
		return "PatchLogDetails [datetime=" + datetime +
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
		PatchLogDetails other = (PatchLogDetails) obj;
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
