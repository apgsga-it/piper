package com.apgsga.microservice.patch.server.impl.persistence;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import com.apgsga.microservice.patch.api.DbModules;
import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.api.ServiceMetaData;
import com.apgsga.microservice.patch.api.ServicesMetaData;
import com.apgsga.microservice.patch.exceptions.PatchServiceRuntimeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public class FilebasedPatchPersistence implements PatchPersistence {

	private static final String SERVICE_META_DATA_JSON = "ServicesMetaData.json";

	private static final String DB_MODULES_JSON = "DbModules.json";

	protected final Log LOGGER = LogFactory.getLog(getClass());

	private Resource storagePath;

	private Resource tempStoragePath;

	public FilebasedPatchPersistence(Resource storagePath, Resource workDir) {
		super();
		this.storagePath = storagePath;
		this.tempStoragePath = workDir;
	}

	public void init() throws IOException {
		if (!storagePath.exists()) {
			// TODO (che, 25.1) : Do we want this? Correct here?
			LOGGER.info("Creating persistence directory: " + storagePath);
			storagePath.getFile().mkdir();
		}

		if (!tempStoragePath.exists()) {
			// TODO (che, 25.1) : Do we want this? Correct here?
			LOGGER.info("Creating Temporary work directory: " + tempStoragePath);
			tempStoragePath.getFile().mkdir();
		}
	}

	@Override
	public synchronized Patch findById(String patchNummer) {
		try {
			File patchFile = createFile("Patch" + patchNummer + ".json");
			if (!patchFile.exists()) {
				return null;
			}
			ObjectMapper mapper = new ObjectMapper();
			Patch patchData = mapper.readValue(patchFile, Patch.class);
			return patchData;
		} catch (IOException e) {
			throw new PatchServiceRuntimeException("Persistence Error for Patchnumber: " + patchNummer, e);
		}
	}

	@Override
	public synchronized Boolean patchExists(String patchNumber) {
		try {
			File patchFile = createFile("Patch" + patchNumber + ".json");
			if (patchFile.exists()) {
				return true;
			}
			return false;
		} catch (IOException e) {
			throw new PatchServiceRuntimeException("Persistence Error on patchExists for Patchnumber: " + patchNumber, e);
		}

	}

	@Override
	public List<String> findAllPatchIds() {
		try {
			File[] files = storagePath.getFile().listFiles(file -> file.getName().startsWith("Patch"));
			return Lists.newArrayList(files).stream().map(f -> FilenameUtils.getBaseName(f.getName()).substring(5))
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new PatchServiceRuntimeException("Persistence Error for finding all Patch ids", e);
		}

	}

	@Override
	public synchronized void savePatch(Patch patch) {
		writeToFile(patch, "Patch" + patch.getPatchNummer() + ".json");
	}

	// TODO (che, 8.5) Do we want remove also "Atomic"
	@Override
	public synchronized void removePatch(Patch patch) {
		try {
			LOGGER.info("Deleting patch: " + patch.toString());
			File patchFile = createFile("Patch" + patch.getPatchNummer() + ".json");
			patchFile.delete();
			LOGGER.info("Deleting patch: " + patch.toString());

		} catch (IOException e) {
			throw new PatchServiceRuntimeException("Persistence Error for removing Patch: " + patch.toString(), e);
		}
	}

	@Override
	public void saveServicesMetaData(ServicesMetaData serviceData) {
		writeToFile(serviceData, SERVICE_META_DATA_JSON);
	}

	@Override
	public ServicesMetaData getServicesMetaData() {
		try {
			File serviceMetaDataFile = createFile(SERVICE_META_DATA_JSON);
			if (!serviceMetaDataFile.exists()) {
				return null;
			}
			ObjectMapper mapper = new ObjectMapper();
			ServicesMetaData result = mapper.readValue(serviceMetaDataFile, ServicesMetaData.class);
			return result;
		} catch (IOException e) {
			throw new PatchServiceRuntimeException("Persistence Error getting ServiceMetadata", e);
		}
	}

	@Override
	public void saveDbModules(DbModules dbModules) {
		writeToFile(dbModules, DB_MODULES_JSON);
	}

	@Override
	public DbModules getDbModules() {
		try {
			File dbModulesFile = createFile(DB_MODULES_JSON);
			if (!dbModulesFile.exists()) {
				return null;
			}
			ObjectMapper mapper = new ObjectMapper();
			DbModules result = mapper.readValue(dbModulesFile, DbModules.class);
			return result;
		} catch (IOException e) {
			throw new PatchServiceRuntimeException("Persistence Error getting DbModules", e);
		}
	}

	@Override
	public List<String> listAllFiles() {
		try {
			File[] listFiles = storagePath.getFile().listFiles();
			return Lists.newArrayList(listFiles).stream().map(f -> f.getName()).collect(Collectors.toList());
		} catch (IOException e) {
			throw new PatchServiceRuntimeException("Persistence Error listing all Files", e);
		}
	}

	@Override
	public List<String> listFiles(String prefix) {
		try {
			File[] listFiles = storagePath.getFile().listFiles();
			return Lists.newArrayList(listFiles).stream().filter(f -> f.getName().startsWith(prefix))
					.map(f -> f.getName()).collect(Collectors.toList());
		} catch (IOException e) {
			throw new PatchServiceRuntimeException("Persistence Error listing all Files with prefix: " + prefix, e);
		}
	}

	@Override
	public void clean() {
		try {
			File parentDir = storagePath.getFile();
			FileUtils.cleanDirectory(parentDir);
		} catch (IOException e) {
			throw new PatchServiceRuntimeException("Persistence Error on clean", e);
		}

	}

	@Override
	public ServiceMetaData findServiceByName(String serviceName) {
		List<ServiceMetaData> services = getServicesMetaData().getServicesMetaData();
		List<ServiceMetaData> result = services.stream().filter(p -> p.getServiceName().equals(serviceName))
				.collect(Collectors.toList());
		Assert.isTrue(result.size() == 1);
		return result.get(0);
	}

	private <T> void writeToFile(T object, String filename) {
		ObjectMapper mapper = new ObjectMapper();
		String jsonRequestString;
		try {
			jsonRequestString = mapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new PatchServiceRuntimeException("Json Processing Error, before Atomic write of File: " + filename, e);
		}
		AtomicFileWriteManager.create(storagePath, tempStoragePath).write(jsonRequestString, filename);

	}

	private File createFile(String fileName) throws IOException {
		File parentDir = storagePath.getFile();
		File revisions = new File(parentDir, fileName);
		return revisions;
	}

}
