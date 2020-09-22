package com.apgsga.microservice.patch.core.impl;

import com.apgsga.microservice.patch.api.Stage;
import com.apgsga.microservice.patch.api.StageMapping;
import com.apgsga.microservice.patch.exceptions.Asserts;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.google.common.collect.Maps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
		Asserts.notNullOrEmpty(patchNumber, "PatchActionExecutorImpl.execute.patchnumber.notnullorempty.assert",
				new Object[] {toStatus });
		Asserts.isTrue((patchContainer.getRepo().patchExists(patchNumber)),
				"PatchActionExecutorImpl.execute.patch.exists.assert", new Object[] { patchNumber, toStatus });

		StageMapping stageMapping = patchContainer.getMetaInfoRepo().stageMappingFor(toStatus);
		Asserts.notNull(stageMapping,"PatchActionExecutorImpl.executePatchAction.state.exits.assert",new String[]{toStatus,patchNumber});
		String toStatusNameWithoutStageMappingName = toStatus.substring(stageMapping.getName().length(),toStatus.length());
		Stage stage = stageMapping.getStages().stream().filter(s -> s.getToState().equals(toStatusNameWithoutStageMappingName)).findFirst().orElse(null);
		Asserts.notNull(stage,"PatchActionExecutorImpl.executePatchAction.state.exits.assert",new String[]{toStatus,patchNumber});

		try {
			Map<String,String> parameters = Maps.newHashMap();
			parameters.put("target",stageMapping.getTarget());
			parameters.put("stage",stage.getName());
			Class<?> clazz = Class.forName(stage.getImplcls());
			Constructor<?> constr = clazz.getConstructor(SimplePatchContainerBean.class);
			Object instance = constr.newInstance(patchContainer);
			Method executeToStateAction = clazz.getMethod("executeToStateAction",String.class, String.class, Map.class);
			String res = (String) executeToStateAction.invoke(instance,new Object[]{patchNumber,toStatus,parameters});
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | ClassNotFoundException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException(
					"PatchActionExecutorImpl.execute.exception",
					new Object[] { e.getMessage(), patchNumber, toStatus }, e);
		}
	}
}
