package com.apgsga.microservice.patch.server.impl.targets;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;

import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class InstallTargetsUtil {

	private InstallTargetsUtil() {
		super();
	}
	
	@SuppressWarnings("unchecked")
	public static List<String> listInstallTargets(Resource targetConfigFile) {

		TargetSystemMappings result;
		ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);;
		try {
			result = objectMapper.readValue(targetConfigFile.getInputStream(), TargetSystemMappings.class);
		} catch (IOException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("InstallTargetsUtil.listInstallTargets.exception",
					new Object[] { e.getMessage(), targetConfigFile.getFilename() }, e);
		}
		
		return result.getOnDemandTarget();
	}

}
