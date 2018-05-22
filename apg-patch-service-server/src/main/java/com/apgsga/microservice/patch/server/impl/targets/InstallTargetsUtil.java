package com.apgsga.microservice.patch.server.impl.targets;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.ObjectMapper;

public class InstallTargetsUtil {

	private InstallTargetsUtil() {
		super();
	}
	
	@SuppressWarnings("unchecked")
	public static List<String> listInstallTargets(Resource targetConfigFile) {

		Map<String, Object> result;
		try {
			result = new ObjectMapper().readValue(targetConfigFile.getInputStream(), HashMap.class);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		List<String> installationTargets = (List<String>) result.get("otherTargetInstances");
		return installationTargets;
	}

}
