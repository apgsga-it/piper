package com.apgsga.microservice.patch.core.impl;

public class PatchActionExecutorFactoryImpl implements PatchActionExecutorFactory {
	

	public PatchActionExecutorFactoryImpl() {
		super();
	}

	@Override
	public PatchActionExecutor create(SimplePatchContainerBean patchContainer) {
		return new PatchActionExecutorImpl(patchContainer);
	}

}
