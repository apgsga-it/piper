package com.apgsga.microservice.patch.core.impl;

public interface PatchActionExecutorFactory {
	
	PatchActionExecutor create(SimplePatchContainerBean patchContainer);

}
