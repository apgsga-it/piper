package com.apgsga.microservice.patch.core.impl.persistence;

import com.apgsga.microservice.patch.api.Package;
import com.apgsga.microservice.patch.api.*;
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

	private static final String SERVICES_META_DATA_JSON = "ServicesMetaData.json";

	private static final String DB_MODULES_JSON = "DbModules.json";

	private static final String ON_DEMAND_TARGETS_DATA_JSON = "OnDemandTargets.json";

	private static final String STAGE_MAPPINGS_DATA_JSON = "StageMappings.json";

	private static final String TARGET_INSTANCES_DATA_JSON = "TargetInstances.json";

	protected static final Log LOGGER = LogFactory.getLog(PatchPersistenceImpl.class.getName());

	private final FilebasedPersistence patchPersistence;
	private final FilebasedPersistence systemMetaDataPersistence;
	private final PatchRdbms patchRdbms;

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
		Asserts.notNullOrEmpty(patchNumber, "Patch Number is null or empty for findById");
		try {
			File patchFile = patchPersistence.createFile(PATCH + patchNumber + JSON);
			return patchPersistence.findFile(patchFile, Patch.class);
		}catch(Exception e) {
			throw ExceptionFactory.create("Persistence Exception  : <%s> for findById for Patchnumber: %s",e,
				 e.getMessage(), patchNumber);
		}
	}
	
	@Override
	public synchronized PatchLog findPatchLogById(String patchNumber) {
		Asserts.notNullOrEmpty(patchNumber, "Patch Number is null or empty for findPatchLogById");
		try {
			File patchFile = patchPersistence.createFile(PATCH_LOG + patchNumber + JSON);
			return patchPersistence.findFile(patchFile, PatchLog.class);
		}catch(Exception e) {
			throw ExceptionFactory.create("Persistence Exception  : <%s> for findPatchLogById for Patchnumber: %s",e,
					e.getMessage(), patchNumber);
		}
	}
	
	@Override
	public synchronized Boolean patchExists(String patchNumber) {
		Asserts.notNullOrEmpty(patchNumber, "Patch Number is null or empty for patchExists");
		try {
			File patchFile = patchPersistence.createFile(PATCH + patchNumber + JSON);
			return patchFile.exists();
		} catch (IOException e) {
			throw ExceptionFactory.create("Persistence Exception  : <%s> for patchExists for Patchnumber: %s",e,
					e.getMessage(), patchNumber);
		}

	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public List<String> findAllPatchIds() {
		try {
			File[] files = patchPersistence.getStoragePath().getFile().listFiles(file -> file.getName().startsWith(PATCH));
			return Lists.newArrayList(files).stream().map(f -> FilenameUtils.getBaseName(f.getName()).substring(5))
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw ExceptionFactory.create("Persistence Exception  : <%s> for findAllPatchIds",e,
					e.getMessage());
		}

	}

	@Override
	public synchronized void savePatch(Patch patch) {
		Asserts.notNull(patch, "Patch object null for save");
		Asserts.notNullOrEmpty(patch.getPatchNumber(), "Patch Number is null or empty for savePatch");
		patchPersistence.writeToFile(patch, PATCH + patch.getPatchNumber() + JSON);
	}
	
	@Override
	public void savePatchLog(String patchNumber, PatchLogDetails logDetails) {
		Asserts.notNull(logDetails, "PatchLogDetails object null for savePatchLog");
		Asserts.notNullOrEmpty(patchNumber, "Patch number null or empty for savePatchLog of Patch");
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
		Asserts.notNull(patch, "Patch object null for remove of Patch");
		Asserts.notNullOrEmpty(patch.getPatchNumber(), "Patch number null or empty for removePatch of Patch");
		Asserts.isTrue((patchExists(patch.getPatchNumber())), "Patch  %s to be removed doesn't exist",patch.getPatchNumber());
		try {
			LOGGER.info("Deleting patch: " + patch.toString());
			File patchFile = patchPersistence.createFile(PATCH + patch.getPatchNumber() + JSON);
			LOGGER.info("Deleting patch: " + patch.toString() + ", result: " + patchFile.delete());

		} catch (IOException e) {
			throw ExceptionFactory.create("Persistance Exception  : <%s> for removePatch",e,
					e.getMessage());
		}
	}

	@Override
	public void saveServicesMetaData(ServicesMetaData serviceData) {
		systemMetaDataPersistence.writeToFile(serviceData, SERVICES_META_DATA_JSON);
	}

	@Override
	public ServicesMetaData getServicesMetaData() {
		try {
			File serviceMetaDataFile = systemMetaDataPersistence.createFile(SERVICES_META_DATA_JSON);
			Asserts.isTrue(serviceMetaDataFile.exists(), "Metadata File %s does not exist in Storage Path %s", SERVICES_META_DATA_JSON, systemMetaDataPersistence.getStoragePath().getFilename());
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(serviceMetaDataFile, ServicesMetaData.class);
		} catch (IOException e) {
			throw ExceptionFactory.create("Persistance Exception  : <%s> for getServicesMetaData",e,
					e.getMessage());
		}
	}

	@Override
	public void saveDbModules(DbModules dbModules) {
		systemMetaDataPersistence.writeToFile(dbModules, DB_MODULES_JSON);
	}

	@Override
	public DbModules getDbModules() {
		try {
			File dbModulesFile = systemMetaDataPersistence.createFile(DB_MODULES_JSON);
			Asserts.isTrue(dbModulesFile.exists(), "Metadata File %s does not exist in Storage Path %s", DB_MODULES_JSON, systemMetaDataPersistence.getStoragePath().getFilename());
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(dbModulesFile, DbModules.class);
		} catch (IOException e) {
			throw ExceptionFactory.create("Persistance Exception  : <%s> for getDbModules",e,
					e.getMessage());
		}
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public List<String> listAllFiles() {
		try {
			File[] listFiles = patchPersistence.getStoragePath().getFile().listFiles();
			return Lists.newArrayList(listFiles).stream().map(File::getName).collect(Collectors.toList());
		} catch (IOException e) {
			throw ExceptionFactory.create("Persistance Exception  : <%s> for listAllFiles",e,
					e.getMessage());
		}
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public List<String> listFiles(String prefix) {
		try {
			File[] listFiles = patchPersistence.getStoragePath().getFile().listFiles();
			return Lists.newArrayList(listFiles).stream().map(File::getName)
					.filter(s -> s.startsWith(prefix)).collect(Collectors.toList());
		} catch (IOException e) {
			throw ExceptionFactory.create("Persistance Exception  : <%s> for listFiles with Prefix: %s",e,
					e.getMessage(),  prefix);
		}
	}

	@Override
	public void clean() {
		try {
			File parentDir = patchPersistence.getStoragePath().getFile();
			FileUtils.cleanDirectory(parentDir);
		} catch (IOException e) {
			throw ExceptionFactory.create("Persistance Exception  : <%s> for clean",e,
					e.getMessage());
		}

	}

	@Override
	public ServiceMetaData getServiceMetaDataByName(String serviceName) {
		List<ServiceMetaData> services = getServicesMetaData().getServicesMetaData();
		List<ServiceMetaData> result = services.stream().filter(p -> p.getServiceName().equals(serviceName))
				.collect(Collectors.toList());
		Asserts.isTrue(result.size() == 1, "No Service Metadata for <%s>",serviceName);
		return result.get(0);
	}

	@Override
	public OnDemandTargets onDemandTargets() {
		try {
			File onDemandTargetFile = systemMetaDataPersistence.createFile(ON_DEMAND_TARGETS_DATA_JSON);
			Asserts.isTrue(onDemandTargetFile.exists(), "Metadata File %s does not exist in Storage Path %s", ON_DEMAND_TARGETS_DATA_JSON, systemMetaDataPersistence.getStoragePath().getFilename());
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(onDemandTargetFile, OnDemandTargets.class);
		} catch (IOException e) {
			throw ExceptionFactory.create("Persistance Exception  : <%s> for onDemandTargets",e,
					e.getMessage());
		}
	}

	@Override
	public StageMappings stageMappings() {
		try {
			File stageMappingFile = systemMetaDataPersistence.createFile(STAGE_MAPPINGS_DATA_JSON);
			Asserts.isTrue(stageMappingFile.exists(), "Metadata File %s does not exist in Storage Path %s", STAGE_MAPPINGS_DATA_JSON, systemMetaDataPersistence.getStoragePath().getFilename());
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(stageMappingFile, StageMappings.class);
		} catch (IOException e) {
			throw ExceptionFactory.create("Persistance Exception  : <%s> for stageMappings",e,
					e.getMessage());
		}
	}

	@Override
	public TargetInstances targetInstances() {
		try {
			File targetInstanceFile = systemMetaDataPersistence.createFile(TARGET_INSTANCES_DATA_JSON);
			Asserts.isTrue(targetInstanceFile.exists(), "Metadata File %s does not exist in Storage Path %s", TARGET_INSTANCES_DATA_JSON, systemMetaDataPersistence.getStoragePath().getFilename());
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(targetInstanceFile, TargetInstances.class);
		} catch (IOException e) {
			throw ExceptionFactory.create("Persistance Exception  : <%s> for targetInstances",e,
					e.getMessage());
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
