package com.apgsga.microservice.patch.core.impl;

import com.apgsga.microservice.patch.api.Stage;
import com.apgsga.microservice.patch.api.StageMapping;
import com.apgsga.microservice.patch.exceptions.Asserts;
import com.google.common.collect.Maps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

public class PatchActionExecutorImpl implements PatchActionExecutor {

	protected static final Log LOGGER = LogFactory.getLog(PatchActionExecutorImpl.class.getName());

	private SimplePatchContainerBean patchContainer;

	public PatchActionExecutorImpl() {
		super();
	}

	public PatchActionExecutorImpl(SimplePatchContainerBean patchContainer) {
		super();
		this.patchContainer = patchContainer;
	}

	@Override
	public void execute(String patchNumber, String toStatus) {
		Asserts.notNullOrEmpty(patchNumber, "PatchActionExecutorImpl.execute.patchnumber.notnullorempty.assert", new Object[] {toStatus });
		Asserts.isTrue((patchContainer.getRepo().patchExists(patchNumber)),"PatchActionExecutorImpl.execute.patch.exists.assert", new Object[] { patchNumber, toStatus });
		StageMapping stageMapping = patchContainer.getMetaInfoRepo().stageMappingFor(toStatus);
		Asserts.notNull(stageMapping,"PatchActionExecutorImpl.executePatchAction.state.exits.assert",new String[]{toStatus,patchNumber});
		String toStatusNameWithoutStageMappingName = toStatus.substring(stageMapping.getName().length(),toStatus.length());
		Stage stage = stageMapping.getStages().stream().filter(s -> s.getToState().equals(toStatusNameWithoutStageMappingName)).findFirst().orElse(null);
		Asserts.notNull(stage,"PatchActionExecutorImpl.executePatchAction.state.exits.assert",new String[]{toStatus,patchNumber});
		Map<String,String> parameters = Maps.newHashMap();
		parameters.put("target",stageMapping.getTarget());
		parameters.put("stage",stage.getName());
		PipelineInputAction pia = new PipelineInputAction(patchContainer);
		// TODO JHE (13.10.2020): pia.executeToStateAction returns a String but .... do we really want to do something with?
		pia.executeToStateAction(patchNumber,toStatus,parameters);
	}
}
