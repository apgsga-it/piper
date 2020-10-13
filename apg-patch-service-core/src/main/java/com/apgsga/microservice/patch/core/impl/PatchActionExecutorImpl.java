package com.apgsga.microservice.patch.core.impl;

import com.apgsga.microservice.patch.api.PatchSystemMetaInfoPersistence;
import com.apgsga.microservice.patch.api.Stage;
import com.apgsga.microservice.patch.api.StageMapping;
import com.apgsga.microservice.patch.api.StageMappings;
import com.apgsga.microservice.patch.exceptions.Asserts;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.google.common.collect.Maps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import java.util.Map;

public class PatchActionExecutorImpl implements PatchActionExecutor {

	protected static final Log LOGGER = LogFactory.getLog(PatchActionExecutorImpl.class.getName());

	public static final String ENTWICKLUNG_INSTALLATIONSBEREIT_ACTION = "EntwicklungInstallationsbereitAction";

	public static final String PIPELINE_INPUT_ACTION = "PipelineInputAction";

	private SimplePatchContainerBean patchContainer;

	private Map<String,PatchAction> patchActions;

	public PatchActionExecutorImpl() {
		super();
	}

	public PatchActionExecutorImpl(SimplePatchContainerBean patchContainer) {
		super();
		this.patchContainer = patchContainer;
		initPatchActions();
	}

	private void initPatchActions() {
		patchActions = Maps.newHashMap();
		Assert.notNull(patchContainer,"Patch Container shouldn't be null");
		PatchSystemMetaInfoPersistence metaInfoRepo = patchContainer.getMetaInfoRepo();
		Assert.notNull(metaInfoRepo,"Patchsystem Metainfo shouldn't be null");
		StageMappings stageMappings = metaInfoRepo.stageMappings();
		Assert.notNull(stageMappings,"Stagemappings  shouldn't be null");
		stageMappings.getStageMappings().forEach(stageMapping -> {
			stageMapping.getStages().forEach(stage -> {
				patchActions.put(stageMapping.getName() + stage.getToState(), getAction(stage.getImplcls()));
			});
		});
	}

	private PatchAction getAction(String implcls) {
		//TODO JHE (13.10.2020): One could do better that if/else ... but ok for now
		if(implcls.contains(ENTWICKLUNG_INSTALLATIONSBEREIT_ACTION)) {
			return new EntwicklungInstallationsbereitAction(patchContainer);
		}
		if(implcls.contains(PIPELINE_INPUT_ACTION)) {
			return new PipelineInputAction(patchContainer);
		}
		throw ExceptionFactory.createPatchServiceRuntimeException("PatchActionExecutorImpl.implcls.exception",new Object[]{implcls});
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
		PatchAction pa = patchActions.get(toStatus);
		pa.executeToStateAction(patchNumber,toStatus,parameters);
	}

}
