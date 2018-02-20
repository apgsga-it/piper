package com.apgsga.microservice.patch.server.impl;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import com.google.common.collect.Maps;

public class ActionContextExecuteStateTransition {

	private static Map<String, Class<? extends ActionExecuteStateTransition>> actions = Maps.newConcurrentMap();

	static {
		actions.put("EntwicklungInstallationsbereit", ActionToEntwicklungInstallationsbereit.class);
		actions.put("Entwicklung", ActionToEntwicklung.class);
		actions.put("Informatiktestinstallation", ActionToInformatiktestinstallation.class);
		actions.put("Produktionsinstallation", ActionToProduktionsinstallation.class);
	}

	private SimplePatchContainerBean patchContainerBean;

	public ActionContextExecuteStateTransition(SimplePatchContainerBean patchContainerBean) {
		super();
		this.patchContainerBean = patchContainerBean;
	}

	public void executeStateTransitionAction(String patchNumber, String toStatus) {
		try {
			Class<? extends ActionExecuteStateTransition> strategyClass = actions.get(toStatus);
			Assert.notNull(strategyClass, "No Strategy found for: " + toStatus);
			Constructor<? extends ActionExecuteStateTransition> constructor = strategyClass
					.getConstructor(SimplePatchContainerBean.class);
			ActionExecuteStateTransition instantiateClass = BeanUtils.instantiateClass(constructor, patchContainerBean);
			instantiateClass.executeStateTransitionAction(patchNumber);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}

	}

}
