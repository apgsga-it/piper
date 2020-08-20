package com.apgsga.microservice.patch.core.impl;

import com.apgsga.system.mapping.api.TargetSystemMapping;

public class PatchActionExecutorFactoryImpl implements PatchActionExecutorFactory {
	
	private TargetSystemMapping tsm;

	public PatchActionExecutorFactoryImpl(TargetSystemMapping tsm) {
		super();
		this.tsm = tsm;
	}

	@Override
	public PatchActionExecutor create(SimplePatchContainerBean patchContainer) {
		return new PatchActionExecutorImpl(tsm, patchContainer);
	}

}
