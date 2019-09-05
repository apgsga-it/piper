package com.apgsga.microservice.patch.server.impl.targets;

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
		Assert.assertTrue(installTargets.remove("dev-bsp"));
		Assert.assertTrue(installTargets.remove("CHEI212"));
		Assert.assertTrue(installTargets.remove("CHTI214"));
		Assert.assertTrue(installTargets.remove("CHQI211"));
		Assert.assertTrue(installTargets.size() == 0);

	}
}
