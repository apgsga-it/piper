package com.apgsga.microservice.patch.server.impl.persistence.utils;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;

import com.apgsga.microservice.patch.api.PatchOpService;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.api.TargetSystemEnviroment;
import com.apgsga.microservice.patch.api.impl.TargetSystemEnviromentBean;
import com.apgsga.microservice.patch.server.impl.persistence.FilebasedPatchPersistence;
import com.google.common.collect.Lists;

public class TargetSystemUtil {

	private static List<TargetSystemEnviroment> installationTargets = Lists.newArrayList();

	static {
		installationTargets.add(new TargetSystemEnviromentBean("CHEI212", "T"));
		installationTargets.add(new TargetSystemEnviromentBean("CHEI211", "T"));
		installationTargets.add(new TargetSystemEnviromentBean("CHTI211", "T"));
		installationTargets.add(new TargetSystemEnviromentBean("CHTI212", "T"));
	}

	public static void main(String[] args) {
		final ResourceLoader rl = new FileSystemResourceLoader();
		final PatchPersistence db = new FilebasedPatchPersistence(rl.getResource("db"));
		db.saveTargetSystemEnviroments(installationTargets);
		List<TargetSystemEnviroment> installTargets = db.getInstallationTargets();
		Assert.isTrue(CollectionUtils.isEqualCollection(installTargets, installationTargets)); 

	}

}
