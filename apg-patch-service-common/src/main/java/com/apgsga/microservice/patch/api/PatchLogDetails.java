package com.apgsga.microservice.patch.api;

import java.util.Date;

import com.affichage.persistence.common.client.EntityRootInterface;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@EntityRootInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
public interface PatchLogDetails {

		public static final String DATETIME = "dateTime";

		public static final String TARGET = "target";

		public static final String PATCH_PIPELINE_TASK = "patchPipelineTask";
		
		public static final String LOG_TEXT = "logText";

		Date getDateTime();

		void setDateTime(Date datetime);

		String getTarget();

		void setTarget(String target);

		String getPatchPipelineTask();

		void setPatchPipelineTask(String patchPipelineTask);
		
		String getLogText();
		
		void setLogText(String logText);
}
