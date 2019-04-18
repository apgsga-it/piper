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

import com.apgsga.microservice.patch.api.DbModules;
import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchLog;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.api.ServiceMetaData;
import com.apgsga.microservice.patch.api.ServicesMetaData;
import com.apgsga.microservice.patch.exceptions.Asserts;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public class FilebasedPatchPersistence implements PatchPersistence {

	private static final String JSON = ".json";

	private static final String PATCH = "Patch";
	
	private static final String PATCH_LOG = "PatchLog";

	private static final String SERVICE_META_DATA_JSON = "ServicesMetaData.json";

	private static final String DB_MODULES_JSON = "DbModules.json";

	protected static final Log LOGGER = LogFactory.getLog(FilebasedPatchPersistence.class.getName());

	private Resource storagePath;

	private Resource tempStoragePath;

	public FilebasedPatchPersistence(Resource storagePath, Resource workDir) {
		super();
		this.storagePath = storagePath;
		this.tempStoragePath = workDir;
	}

	public void init() throws IOException {
		if (!storagePath.exists()) {
			LOGGER.info("Creating persistence directory: " + storagePath);
			storagePath.getFile().mkdir();
		}

		if (!tempStoragePath.exists()) {
			LOGGER.info("Creating Temporary work directory: " + tempStoragePath);
			tempStoragePath.getFile().mkdir();
		}
	}

	@Override
	public synchronized Patch findById(String patchNummer) {
		Asserts.notNullOrEmpty(patchNummer, "FilebasedPatchPersistence.findById.patchnumber.notnullorempty.assert",new Object[] {});
		try {
			File patchFile = createFile(PATCH + patchNummer + JSON);
			return findFile(patchFile, Patch.class);
		}catch(Exception e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("FilebasedPatchPersistence.findById.exception",
				new Object[] { e.getMessage(), patchNummer }, e);
		}
	}
	
	@Override
	public synchronized PatchLog findPatchLogById(String patchNummer) {
		Asserts.notNullOrEmpty(patchNummer, "FilebasedPatchPersistence.findById.patchlognumber.notnullorempty.assert", new Object[] {});
		try {
			File patchFile = createFile(PATCH_LOG + patchNummer + JSON);
			return findFile(patchFile, PatchLog.class);
		}catch(Exception e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("FilebasedPatchPersistence.findById.exception",
				new Object[] { e.getMessage(), patchNummer }, e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T> T findFile(File f, Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
			if (!f.exists()) {
				return null;
			}
			ObjectMapper mapper = new ObjectMapper();
			T patchData = (T) mapper.readValue(f, clazz);
			return patchData;
	}

	@Override
	public synchronized Boolean patchExists(String patchNumber) {
		Asserts.notNullOrEmpty(patchNumber, "FilebasedPatchPersistence.patchExists.patchnumber.notnullorempty.assert",
				new Object[] {});
		try {
			File patchFile = createFile(PATCH + patchNumber + JSON);
			return patchFile.exists();
		} catch (IOException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("FilebasedPatchPersistence.patchExists.exception",
					new Object[] { e.getMessage(), patchNumber }, e);
		}

	}

	@Override
	public List<String> findAllPatchIds() {
		try {
			File[] files = storagePath.getFile().listFiles(file -> file.getName().startsWith(PATCH));
			return Lists.newArrayList(files).stream().map(f -> FilenameUtils.getBaseName(f.getName()).substring(5))
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException(
					"FilebasedPatchPersistence.findAllPatchIds.exception", new Object[] { e.getMessage() }, e);
		}

	}

	@Override
	public synchronized void savePatch(Patch patch) {
		Asserts.notNull(patch, "FilebasedPatchPersistence.save.patchobject.notnull.assert", new Object[] {});
		Asserts.notNullOrEmpty(patch.getPatchNummer(),
				"FilebasedPatchPersistence.save.patchnumber.notnullorempty.assert", new Object[] { patch.toString() });
		writeToFile(patch, PATCH + patch.getPatchNummer() + JSON);
	}
	
	@Override
	public void savePatchLog(PatchLog patchLog) {
		Asserts.notNull(patchLog, "FilebasedPatchPersistence.save.patchlogobject.notnull.assert", new Object[] {});
		Asserts.notNullOrEmpty(patchLog.getPatchNumber(), "FilebasedPatchPersistence.save.patchlognumber.notnullorempty.assert", new Object[] {patchLog.toString()});
		writeToFile(patchLog, PATCH_LOG + patchLog.getPatchNumber() + JSON);
	}

	// TODO (che, 8.5) Do we want remove also "Atomic"
	@Override
	public synchronized void removePatch(Patch patch) {
		Asserts.notNull(patch, "FilebasedPatchPersistence.remove.patchobject.notnull.assert", new Object[] {});
		Asserts.notNullOrEmpty(patch.getPatchNummer(),
				"FilebasedPatchPersistence.remove.patchnumber.notnullorempty.assert",
				new Object[] { patch.toString() });
		Asserts.isTrue((patchExists(patch.getPatchNummer())), "FilebasedPatchPersistence.remove.patch.exists.assert",
				new Object[] { patch.toString() });
		try {
			LOGGER.info("Deleting patch: " + patch.toString());
			File patchFile = createFile(PATCH + patch.getPatchNummer() + JSON);
			LOGGER.info("Deleting patch: " + patch.toString() + ", result: " + patchFile.delete());

		} catch (IOException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("FilebasedPatchPersistence.removePatch.exception",
					new Object[] { e.getMessage(), patch.toString() }, e);
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
			throw ExceptionFactory.createPatchServiceRuntimeException(
					"FilebasedPatchPersistence.getServicesMetaData.exception", new Object[] { e.getMessage() }, e);
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
			throw ExceptionFactory.createPatchServiceRuntimeException(
					"FilebasedPatchPersistence.getDbModules.exception", new Object[] { e.getMessage() }, e);
		}
	}

	@Override
	public List<String> listAllFiles() {
		try {
			File[] listFiles = storagePath.getFile().listFiles();
			return Lists.newArrayList(listFiles).stream().map(f -> f.getName()).collect(Collectors.toList());
		} catch (IOException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException(
					"FilebasedPatchPersistence.listAllFiles.exception", new Object[] { e.getMessage() }, e);
		}
	}

	@Override
	public List<String> listFiles(String prefix) {
		try {
			File[] listFiles = storagePath.getFile().listFiles();
			return Lists.newArrayList(listFiles).stream().filter(f -> f.getName().startsWith(prefix))
					.map(f -> f.getName()).collect(Collectors.toList());
		} catch (IOException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("FilebasedPatchPersistence.listFiles.exception",
					new Object[] { e.getMessage(), prefix }, e);
		}
	}

	@Override
	public void clean() {
		try {
			File parentDir = storagePath.getFile();
			FileUtils.cleanDirectory(parentDir);
		} catch (IOException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("FilebasedPatchPersistence.clean.exception",
					new Object[] { e.getMessage() }, e);
		}

	}

	@Override
	public ServiceMetaData findServiceByName(String serviceName) {
		List<ServiceMetaData> services = getServicesMetaData().getServicesMetaData();
		List<ServiceMetaData> result = services.stream().filter(p -> p.getServiceName().equals(serviceName))
				.collect(Collectors.toList());
		Asserts.isTrue(result.size() == 1, "FilebasedPatchPersistence.findServiceByName.exception", new Object[] {});
		return result.get(0);
	}

	private <T> void writeToFile(T object, String filename) {
		ObjectMapper mapper = new ObjectMapper();
		String jsonRequestString;
		try {
			jsonRequestString = mapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("FilebasedPatchPersistence.writeToFile.exception",
					new Object[] { e.getMessage(), filename }, e);
		}
		AtomicFileWriteManager.create(this).write(jsonRequestString, filename);

	}

	private File createFile(String fileName) throws IOException {
		File parentDir = storagePath.getFile();
		File revisions = new File(parentDir, fileName);
		return revisions;
	}

	public Resource getStoragePath() {
		return storagePath;
	}

	public Resource getTempStoragePath() {
		return tempStoragePath;
	}
}
