package com.apgsga.microservice.patch.server.impl.deprecated;

import com.apgsga.microservice.patch.server.impl.PatchActionExecutor;
import com.apgsga.microservice.patch.server.impl.PatchActionExecutorFactory;
import com.apgsga.microservice.patch.server.impl.SimplePatchContainerBean;

public class DefaultPatchActionExecutorFactory implements PatchActionExecutorFactory {

	@Override
	public PatchActionExecutor create(SimplePatchContainerBean bean) {
		 return new ActionContextExecuteStateTransition(bean); 
	}

}
