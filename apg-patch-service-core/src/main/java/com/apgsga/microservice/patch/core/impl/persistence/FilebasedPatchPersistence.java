package com.apgsga.microservice.patch.core.impl.persistence;

import com.apgsga.microservice.patch.api.*;
import com.apgsga.microservice.patch.exceptions.Asserts;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class FilebasedPatchPersistence extends AbstractFilebasedPersistence implements PatchPersistence {

	private static final String JSON = ".json";

	private static final String PATCH = "Patch";
	
	private static final String PATCH_LOG = "PatchLog";

	private static final String SERVICE_META_DATA_JSON = "ServicesMetaData.json";

	private static final String DB_MODULES_JSON = "DbModules.json";

	protected static final Log LOGGER = LogFactory.getLog(FilebasedPatchPersistence.class.getName());

	public FilebasedPatchPersistence(Resource storagePath, Resource workDir) throws IOException {
		super();
		this.storagePath = storagePath;
		this.tempStoragePath = workDir;
		init();
	}

	@Override
	public synchronized Patch findById(String patchNumber) {
		Asserts.notNullOrEmpty(patchNumber, "FilebasedPatchPersistence.findById.patchnumber.notnullorempty.assert",new Object[] {});
		try {
			File patchFile = createFile(PATCH + patchNumber + JSON);
			return findFile(patchFile, Patch.class);
		}catch(Exception e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("FilebasedPatchPersistence.findById.exception",
				new Object[] { e.getMessage(), patchNumber}, e);
		}
	}
	
	@Override
	public synchronized PatchLog findPatchLogById(String patchNumber) {
		Asserts.notNullOrEmpty(patchNumber, "FilebasedPatchPersistence.findById.patchlognumber.notnullorempty.assert", new Object[] {});
		try {
			File patchFile = createFile(PATCH_LOG + patchNumber + JSON);
			return findFile(patchFile, PatchLog.class);
		}catch(Exception e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("FilebasedPatchPersistence.findById.exception",
				new Object[] { e.getMessage(), patchNumber}, e);
		}
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
			final List<String> collect = Lists.newArrayList(files).stream().map(f -> FilenameUtils.getBaseName(f.getName()).substring(5))
					.collect(Collectors.toList());
			return collect;
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
		writeToFile(patch, PATCH + patch.getPatchNummer() + JSON, this);
	}
	
	@Override
	public void savePatchLog(String patchNumber) {
		Asserts.notNullOrEmpty(patchNumber, "FilebasedPatchPersistence.save.patchlognumber.notnullorempty.assert", new Object[] {patchNumber});
		PatchLog patchLog = findPatchLogById(patchNumber);
		if(patchLog == null) {
			patchLog = createPatchLog(patchNumber);
		}
		patchLog.addLog(createPatchLogDetail(patchNumber));
		writeToFile(patchLog, PATCH_LOG + patchLog.getPatchNumber() + JSON, this);
	}
	
	private PatchLogDetails createPatchLogDetail(String patchNumber) {
		Patch patch = findById(patchNumber);
		PatchLogDetails pld = new PatchLogDetails();
		pld.setLogText(patch.getLogText());
		pld.setPatchPipelineTask(patch.getCurrentPipelineTask());
		pld.setTarget(patch.getCurrentTarget());
		pld.setDateTime(new Date());
		return pld;
	}

	private PatchLog createPatchLog(String patchNumber) {
		PatchLog pl = new PatchLog();
		pl.setPatchNumber(patchNumber);
		return pl;
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
		writeToFile(serviceData, SERVICE_META_DATA_JSON, this);
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
		writeToFile(dbModules, DB_MODULES_JSON, this);
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
			final List<String> collect = Lists.newArrayList(listFiles).stream().map(File::getName).collect(Collectors.toList());
			return collect;
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
					.map(File::getName).collect(Collectors.toList());
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
}
