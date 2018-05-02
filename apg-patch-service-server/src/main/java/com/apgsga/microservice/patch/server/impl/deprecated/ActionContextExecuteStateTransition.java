package com.apgsga.microservice.patch.server.impl.deprecated;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import com.apgsga.microservice.patch.server.impl.PatchActionExecutor;
import com.apgsga.microservice.patch.server.impl.SimplePatchContainerBean;
import com.google.common.collect.Maps;

public class ActionContextExecuteStateTransition implements PatchActionExecutor {

	private static Map<String, Class<? extends ActionExecuteStateTransition>> actions = Maps.newConcurrentMap();

	static {
		actions.put("EntwicklungInstallationsbereit", ActionToEntwicklungInstallationsbereit.class);
		actions.put("InformatiktestInstallationsbereit", ActionToInformatiktestInstallationsbereit.class);
		actions.put("ProduktionInstallationsbereit", ActionToProduktionInstallationsbereit.class);
		actions.put("Entwicklung", ActionToEntwicklung.class);
		actions.put("Informatiktest", ActionToInformatiktest.class);
		actions.put("Produktion", ActionToProduktion.class);
	}

	private SimplePatchContainerBean patchContainerBean;

	public ActionContextExecuteStateTransition(SimplePatchContainerBean patchContainerBean) {
		super();
		this.patchContainerBean = patchContainerBean;
	}

	public void execute(String patchNumber, String toStatus) {
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
