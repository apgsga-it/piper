package com.apgsga.microservice.patch.core.impl.targets;

import com.apgsga.system.mapping.impl.TargetSystemMappingImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@TestPropertySource(locations = "application-test.properties")
@ContextConfiguration(classes = TargetSystemMappingImpl.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class InstallTargetsTest {

	@Autowired
	TargetSystemMappingImpl tsm;
	
	@Test
	public void testListTargets() {
		Assert.assertNotNull(tsm);
		List<String> installTargets = tsm.listInstallTargets();
		Assert.assertTrue(installTargets.size() == 4);
		Assert.assertTrue(installTargets.remove("dev-dro"));
		Assert.assertTrue(installTargets.remove("dev-stb"));
		Assert.assertTrue(installTargets.remove("DEV-CHEI212"));
		Assert.assertTrue(installTargets.remove("DEV-CHTI216"));
		Assert.assertTrue(installTargets.size() == 0);

	}
}
