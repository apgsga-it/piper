package com.apgsga.microservice.patch.core.impl;

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
		// TODO JHE (21.09.2020) : before tsm.stateMap() was called -> need to do the equivalent
		Map<String, Map<String, String>> stateMap = null; //tsm.stateMap();
		Map<String, String> bean = stateMap.get(toStatus);
		Asserts.notNull(bean,"PatchActionExecutorImpl.executePatchAction.state.exits.assert",new String[]{toStatus,patchNumber});
		try {
			Map<String,String> parameters = Maps.newHashMap();
			parameters.put("targetName",bean.get("targetName"));
			parameters.put("target",bean.get("target"));
			parameters.put("stage",bean.get("stage"));
			Class<?> clazz = Class.forName(bean.get("clsName"));
			Constructor<?> constr = clazz.getConstructor(SimplePatchContainerBean.class);
			Object instance = constr.newInstance(patchContainer);
			Method executeToStateAction = clazz.getMethod("executeToStateAction",String.class, String.class, Map.class);
			String res = (String) executeToStateAction.invoke(instance,new Object[]{patchNumber,toStatus,parameters});
		} catch (InstantiationException |ClassNotFoundException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException(
					"PatchActionExecutorImpl.execute.exception",
					new Object[] { e.getMessage(), patchNumber, toStatus }, e);
		}
	}
}
