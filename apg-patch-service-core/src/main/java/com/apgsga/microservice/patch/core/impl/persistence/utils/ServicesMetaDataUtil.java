package com.apgsga.microservice.patch.core.impl.persistence.utils;

import java.util.List;

import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.ResourceLoader;

import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.api.ServiceMetaData;
import com.apgsga.microservice.patch.api.ServicesMetaData;
import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.core.impl.persistence.FilebasedPatchPersistence;
import com.google.common.collect.Lists;

import junit.framework.Assert;

public class ServicesMetaDataUtil {

	private static List<ServiceMetaData> serviceList = Lists.newArrayList();
	static {

		MavenArtifact it21UiStarter = new MavenArtifact();
		it21UiStarter.setArtifactId("it21ui-app-starter");
		it21UiStarter.setGroupId("com.apgsga.it21.ui.mdt");
		it21UiStarter.setName("it21ui-app-starter");

		MavenArtifact jadasStarter = new MavenArtifact();
		jadasStarter.setArtifactId("jadas-app-starter");
		jadasStarter.setGroupId("com.apgsga.it21.ui.mdt");
		jadasStarter.setName("jadas-app-starter");

		final ServiceMetaData it21Ui = new ServiceMetaData("It21Ui", "it21_release_9_1_0_admin_uimig", "9.1.0",
				"ADMIN-UIMIG");
		serviceList.add(it21Ui);
		final ServiceMetaData someOtherService = new ServiceMetaData("SomeOtherService",
				"it21_release_9_1_0_some_tag", "9.1.0", "SOME-TAG");
		serviceList.add(someOtherService);
	}

	public static void main(String[] args) {
		final ResourceLoader rl = new FileSystemResourceLoader();
		final PatchPersistence db = new FilebasedPatchPersistence(rl.getResource("db"), rl.getResource("work"));
		final ServicesMetaData data = new ServicesMetaData();
		data.setServicesMetaData(serviceList);
		db.saveServicesMetaData(data);
		ServicesMetaData serviceData = db.getServicesMetaData();
		Assert.assertEquals(serviceData, data);

	}

}
