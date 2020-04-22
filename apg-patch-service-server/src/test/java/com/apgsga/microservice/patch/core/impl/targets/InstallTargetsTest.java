package com.apgsga.microservice.patch.core.impl.targets;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;


public class InstallTargetsTest {
	
	@Test
	public void testListTargets() {
		ResourceLoader rl = new FileSystemResourceLoader();
		Resource targetConfigFile = rl.getResource("src/test/resources/json/TargetSystemMappings.json"); 
		List<String> installTargets = InstallTargetsUtil.listInstallTargets(targetConfigFile);
		Assert.assertTrue(installTargets.size() == 4);
		Assert.assertTrue(installTargets.remove("dev-dro"));
		Assert.assertTrue(installTargets.remove("dev-stb"));
		Assert.assertTrue(installTargets.remove("DEV-CHEI212"));
		Assert.assertTrue(installTargets.remove("DEV-CHTI216"));
		Assert.assertTrue(installTargets.size() == 0);

	}
}
