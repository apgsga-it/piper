package com.apgsga.microservice.patch.server.impl;

public interface PatchActionExecutorFactory {
	
	PatchActionExecutor create(SimplePatchContainerBean patchContainer);

}
