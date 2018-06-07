package com.apgsga.microservice.patch.server.impl.persistence.utils;

import java.util.List;

import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;

import com.apgsga.microservice.patch.api.DbModules;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.server.impl.persistence.FilebasedPatchPersistence;
import com.google.common.collect.Lists;

public class DbModulesUtil {

	private static List<String> dbModulesList = Lists.newArrayList("testdbmodule", "testdbAnotherdbModule");

	public static void main(String[] args) {
		final ResourceLoader rl = new FileSystemResourceLoader();
		final PatchPersistence db = new FilebasedPatchPersistence(rl.getResource("db"),rl.getResource("work"));
		DbModules intialLoad = new DbModules(dbModulesList);
		db.saveDbModules(intialLoad);
		DbModules dbModules = db.getDbModules();
		Assert.isTrue(intialLoad.equals(dbModules));

	}

}
