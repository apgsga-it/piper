package com.apgsga.microservice.patch.core.impl.persistence;

import com.apgsga.microservice.patch.api.*;
import com.apgsga.microservice.patch.api.Package;
import com.apgsga.microservice.patch.exceptions.Asserts;
import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import com.apgsga.patch.db.integration.api.PatchRdbms;
import com.apgsga.patch.db.integration.impl.PatchRdbmsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class PatchPersistenceImpl implements PatchPersistence {

	private static final String JSON = ".json";

	private static final String PATCH = "Patch";
	
	private static final String PATCH_LOG = "PatchLog";

	private static final String SERVICE_META_DATA_JSON = "ServicesMetaData.json";

	private static final String DB_MODULES_JSON = "DbModules.json";

	private static final String ON_DEMAND_TARGETS_DATA_JSON = "OnDemandTargets.json";

	private static final String STAGE_MAPPINGS_DATA_JSON = "StageMappings.json";

	private static final String TARGET_INSTANCES_DATA_JSON = "TargetInstances.json";

	private static final String SERVICES_METADATA_DATA_JSON = "ServicesMetaData.json";

	protected static final Log LOGGER = LogFactory.getLog(PatchPersistenceImpl.class.getName());

	private FilebasedPersistence patchPersistence;
	private FilebasedPersistence systemMetaDataPersistence;
	private PatchRdbms patchRdbms;

	public PatchPersistenceImpl(Resource storagePath, Resource workDir) throws IOException {
		super();
		this.patchPersistence = FilebasedPersistenceImpl.create(storagePath, workDir);
		this.systemMetaDataPersistence = FilebasedPersistenceImpl.create(storagePath, workDir);
		this.patchRdbms = new PatchRdbmsImpl();
		this.patchPersistence.init();
		this.systemMetaDataPersistence.init();
	}

	public PatchPersistenceImpl(Resource storagePath, Resource workDir,PatchRdbms patchRdbms) throws IOException {
		super();
		this.patchPersistence = FilebasedPersistenceImpl.create(storagePath, workDir);
		this.systemMetaDataPersistence = FilebasedPersistenceImpl.create(storagePath, workDir);
		this.patchRdbms = patchRdbms;
		this.patchPersistence.init();
		this.systemMetaDataPersistence.init();
	}

	public PatchPersistenceImpl(Resource storagePath, Resource storagePathMeta, Resource workDir) throws IOException {
		super();
		this.patchPersistence = FilebasedPersistenceImpl.create(storagePath, workDir);
		this.systemMetaDataPersistence = FilebasedPersistenceImpl.create(storagePathMeta, workDir);
		this.patchRdbms = new PatchRdbmsImpl();;
		this.patchPersistence.init();
		this.systemMetaDataPersistence.init();
	}

	public PatchPersistenceImpl(Resource storagePath, Resource storagePathMeta, Resource workDir, PatchRdbms patchRdbms) throws IOException {
		super();
		this.patchPersistence = FilebasedPersistenceImpl.create(storagePath, workDir);
		this.systemMetaDataPersistence = FilebasedPersistenceImpl.create(storagePathMeta, workDir);
		this.patchRdbms = patchRdbms;
		this.patchPersistence.init();
		this.systemMetaDataPersistence.init();
	}

	@Override
	public synchronized Patch findById(String patchNumber) {
		Asserts.notNullOrEmpty(patchNumber, "FilebasedPatchPersistence.findById.patchnumber.notnullorempty.assert",new Object[] {});
		try {
			File patchFile = patchPersistence.createFile(PATCH + patchNumber + JSON);
			return patchPersistence.findFile(patchFile, Patch.class);
		}catch(Exception e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("FilebasedPatchPersistence.findById.exception",
				new Object[] { e.getMessage(), patchNumber}, e);
		}
	}
	
	@Override
	public synchronized PatchLog findPatchLogById(String patchNumber) {
		Asserts.notNullOrEmpty(patchNumber, "FilebasedPatchPersistence.findById.patchlognumber.notnullorempty.assert", new Object[] {});
		try {
			File patchFile = patchPersistence.createFile(PATCH_LOG + patchNumber + JSON);
			return patchPersistence.findFile(patchFile, PatchLog.class);
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
			File patchFile = patchPersistence.createFile(PATCH + patchNumber + JSON);
			return patchFile.exists();
		} catch (IOException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("FilebasedPatchPersistence.patchExists.exception",
					new Object[] { e.getMessage(), patchNumber }, e);
		}

	}

	@Override
	public List<String> findAllPatchIds() {
		try {
			File[] files = patchPersistence.getStoragePath().getFile().listFiles(file -> file.getName().startsWith(PATCH));
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
		Asserts.notNullOrEmpty(patch.getPatchNumber(),
				"FilebasedPatchPersistence.save.patchnumber.notnullorempty.assert", new Object[] { patch.toString() });
		patchPersistence.writeToFile(patch, PATCH + patch.getPatchNumber() + JSON);
	}
	
	@Override
	public void savePatchLog(String patchNumber, PatchLogDetails logDetails) {
		Asserts.notNull(logDetails,"FilebasedPatchPersistence.save.patchlog.null.loginfo",new Object[] {});
		Asserts.notNullOrEmpty(patchNumber, "FilebasedPatchPersistence.save.patchlognumber.notnullorempty.assert", new Object[] {patchNumber});
		PatchLog patchLog = findPatchLogById(patchNumber);
		PatchLog patchLogToWrite;
		if(patchLog == null) {
			patchLogToWrite = PatchLog.builder().patchNumber(patchNumber).logDetails(Lists.newArrayList(logDetails)).build();
		} else {
			List<PatchLogDetails> logDetailsList = patchLog.getLogDetails();
			logDetailsList.add(logDetails);
			patchLogToWrite = patchLog.toBuilder().logDetails(logDetailsList).build();
		}
		patchPersistence.writeToFile(patchLogToWrite, PATCH_LOG + patchLogToWrite.getPatchNumber() + JSON);
	}


	// TODO (che, 8.5) Do we want remove also "Atomic"
	@Override
	public synchronized void removePatch(Patch patch) {
		Asserts.notNull(patch, "FilebasedPatchPersistence.remove.patchobject.notnull.assert", new Object[] {});
		Asserts.notNullOrEmpty(patch.getPatchNumber(),
				"FilebasedPatchPersistence.remove.patchnumber.notnullorempty.assert",
				new Object[] { patch.toString() });
		Asserts.isTrue((patchExists(patch.getPatchNumber())), "FilebasedPatchPersistence.remove.patch.exists.assert",
				new Object[] { patch.toString() });
		try {
			LOGGER.info("Deleting patch: " + patch.toString());
			File patchFile = patchPersistence.createFile(PATCH + patch.getPatchNumber() + JSON);
			LOGGER.info("Deleting patch: " + patch.toString() + ", result: " + patchFile.delete());

		} catch (IOException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("FilebasedPatchPersistence.removePatch.exception",
					new Object[] { e.getMessage(), patch.toString() }, e);
		}
	}

	@Override
	public void saveServicesMetaData(ServicesMetaData serviceData) {
		systemMetaDataPersistence.writeToFile(serviceData, SERVICE_META_DATA_JSON);
	}

	@Override
	public ServicesMetaData getServicesMetaData() {
		try {
			File serviceMetaDataFile = systemMetaDataPersistence.createFile(SERVICE_META_DATA_JSON);
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
		patchPersistence.writeToFile(dbModules, DB_MODULES_JSON);
	}

	@Override
	public DbModules getDbModules() {
		try {
			File dbModulesFile = patchPersistence.createFile(DB_MODULES_JSON);
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
			File[] listFiles = patchPersistence.getStoragePath().getFile().listFiles();
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
			File[] listFiles = patchPersistence.getStoragePath().getFile().listFiles();
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
			File parentDir = patchPersistence.getStoragePath().getFile();
			FileUtils.cleanDirectory(parentDir);
		} catch (IOException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("FilebasedPatchPersistence.clean.exception",
					new Object[] { e.getMessage() }, e);
		}

	}

	@Override
	public ServiceMetaData getServiceMetaDataByName(String serviceName) {
		List<ServiceMetaData> services = getServicesMetaData().getServicesMetaData();
		List<ServiceMetaData> result = services.stream().filter(p -> p.getServiceName().equals(serviceName))
				.collect(Collectors.toList());
		Asserts.isTrue(result.size() == 1, "FilebasedPatchPersistence.findServiceByName.exception", new Object[] {});
		return result.get(0);
	}

	@Override
	public OnDemandTargets onDemandTargets() {
		try {
			File onDemandTargetFile = systemMetaDataPersistence.createFile(ON_DEMAND_TARGETS_DATA_JSON);
			if (!onDemandTargetFile.exists()) {
				return null;
			}
			ObjectMapper mapper = new ObjectMapper();
			OnDemandTargets result = mapper.readValue(onDemandTargetFile, OnDemandTargets.class);
			return result;
		} catch (IOException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException(
					"FilebasedPatchPersistence.onDemandTargets.exception", new Object[] { e.getMessage() }, e);
		}
	}

	@Override
	public StageMappings stageMappings() {
		try {
			File stageMappingFile = systemMetaDataPersistence.createFile(STAGE_MAPPINGS_DATA_JSON);
			if (!stageMappingFile.exists()) {
				return null;
			}
			ObjectMapper mapper = new ObjectMapper();
			StageMappings result = mapper.readValue(stageMappingFile, StageMappings.class);
			return result;
		} catch (IOException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException(
					"FilebasedPatchPersistence.stageMapping.exception", new Object[] { e.getMessage() }, e);
		}
	}

	@Override
	public TargetInstances targetInstances() {
		try {
			File targetInstanceFile = systemMetaDataPersistence.createFile(TARGET_INSTANCES_DATA_JSON);
			if (!targetInstanceFile.exists()) {
				return null;
			}
			ObjectMapper mapper = new ObjectMapper();
			TargetInstances result = mapper.readValue(targetInstanceFile, TargetInstances.class);
			return result;
		} catch (IOException e) {
			throw ExceptionFactory.createPatchServiceRuntimeException(
					"FilebasedPatchPersistence.targetInstance.exception", new Object[] { e.getMessage() }, e);
		}
	}


	@Override
	public List<Package> packagesFor(Service service) {
		for(ServiceMetaData smd : getServicesMetaData().getServicesMetaData()) {
			if(smd.getServiceName().equals(service.getServiceName())) {
				return smd.getPackages();
			}
		}
		return null;
	}

	@Override
	public String targetFor(String stageName) {
		StageMappings sms = stageMappings();
		for(StageMapping sm : sms.getStageMappings()) {
			if(sm.getName().equalsIgnoreCase(stageName)) {
				return sm.getTarget();
			}
		}
		return null;
	}

	@Override
	public List<String> patchIdsForStatus(String statusCode) {
		return patchRdbms.patchIdsForStatus(statusCode);
	}

	@Override
	public void notify(NotificationParameters params) {
		patchRdbms.notify(params);
	}
}
