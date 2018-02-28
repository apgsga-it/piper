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
import com.apgsga.microservice.patch.api.TargetSystemEnviroment;
import com.apgsga.microservice.patch.api.TargetSystemEnvironments;
import com.apgsga.microservice.patch.api.impl.TargetSystemEnviromentBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.Lists;

public class FilebasedPatchPersistence implements PatchPersistence {

	private static final String SERVICE_META_DATA_JSON = "ServicesMetaData.json";

	private static final String INSTALLATION_TARGETS_JSON = "InstallationTargets.json";

	private static final String DB_MODULES_JSON = "DbModules.json";

	protected final Log LOGGER = LogFactory.getLog(getClass());

	private Resource storagePath;

	public FilebasedPatchPersistence(Resource storagePath) {
		super();
		this.storagePath = storagePath;
	}

	public void init() throws IOException {
		if (!storagePath.exists()) {
			// TODO (che, 25.1) : Do we want this? Correct here?
			LOGGER.info("Creating persistence directory: " + storagePath);
			storagePath.getFile().mkdir();
		}
	}

	@Override
	public synchronized Patch findById(String patchNummer) {
		try {
			File patchFile = createPatchFile(patchNummer);
			if (!patchFile.exists()) {
				return null;
			}
			ObjectMapper mapper = new ObjectMapper();
			Patch patchData = mapper.readValue(patchFile, Patch.class);
			return patchData;
		} catch (IOException e) {
			throw new RuntimeException("Persistence Error", e);
		}
	}

	@Override
	public synchronized Boolean patchExists(String patchNumber) {
		try {
			File patchFile = createPatchFile(patchNumber);
			if (patchFile.exists()) {
				return true;
			}
			return false;
		} catch (IOException e) {
			throw new RuntimeException("Persistence Error", e);
		}

	}

	@Override
	public List<String> findAllPatchIds() {
		try {
			File[] files = storagePath.getFile().listFiles(file -> file.getName().startsWith("Patch"));
			return Lists.newArrayList(files).stream().map(f -> FilenameUtils.getBaseName(f.getName()).substring(5))
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public synchronized void savePatch(Patch patch) {
		try {
			File patchFile = createPatchFile(patch.getPatchNummer());
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(patchFile, patch);

		} catch (IOException e) {
			throw new RuntimeException("Persistence Error", e);
		}

	}

	@Override
	public synchronized void removePatch(Patch patch) {
		try {
			File patchFile = createPatchFile(patch.getPatchNummer());
			patchFile.delete();

		} catch (IOException e) {
			throw new RuntimeException("Persistence Error", e);
		}
	}

	@Override
	public void saveServicesMetaData(ServicesMetaData serviceData) {
		try {
			File patchFile = createServiceDataFile();
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(patchFile, serviceData);
		} catch (IOException e) {
			throw new RuntimeException("Persistence Error", e);
		}

	}

	@Override
	public ServicesMetaData getServicesMetaData() {
		try {
			File serviceData = createServiceDataFile();
			if (!serviceData.exists()) {
				return null;
			}
			ObjectMapper mapper = new ObjectMapper();
			ServicesMetaData result = mapper.readValue(serviceData, ServicesMetaData.class);
			return result;
		} catch (IOException e) {
			throw new RuntimeException("Persistence Error", e);
		}
	}

	@Override
	public void saveTargetSystemEnviroments(List<TargetSystemEnviroment> installationTargets) {
		try {
			File installTargets = createInstallationTargetFile();
			ObjectMapper mapper = new ObjectMapper();
			ObjectWriter writerFor = mapper.writerFor(TargetSystemEnviroment[].class); 
			TargetSystemEnviroment[] array = new TargetSystemEnviroment[installationTargets.size()]; 
			writerFor.writeValue(installTargets, installationTargets.toArray(array));
		} catch (IOException e) {
			throw new RuntimeException("Persistence Error", e);
		}
	}

	@Override
	public List<TargetSystemEnviroment> getInstallationTargets() {
		try {
			File installTargets = createInstallationTargetFile();
			ObjectMapper mapper = new ObjectMapper();
			TargetSystemEnviroment[] result = mapper.readValue(installTargets, TargetSystemEnviroment[].class);
			return Lists.newArrayList(result);
		} catch (IOException e) {
			throw new RuntimeException("Persistence Error", e);
		}
	}

	@Override
	public void saveTargetSystemEnviroments(TargetSystemEnvironments targets) {
		saveTargetSystemEnviroments(targets.getTargetSystemEnviroments());
	}

	@Override
	public TargetSystemEnvironments getTargetSystemEnviroments() {
		return new TargetSystemEnvironments(getInstallationTargets());
	}

	@Override
	public void saveDbModules(DbModules dbModules) {
		try {
			File dbModulesFile = createDbModulesFile();
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(dbModulesFile, dbModules);
		} catch (IOException e) {
			throw new RuntimeException("Persistence Error", e);
		}
	}

	@Override
	public DbModules getDbModules() {
		try {
			File dbModulesFile = createDbModulesFile();
			if (!dbModulesFile.exists()) {
				return null;
			}
			ObjectMapper mapper = new ObjectMapper();
			DbModules result = mapper.readValue(dbModulesFile, DbModules.class);
			return result;
		} catch (IOException e) {
			throw new RuntimeException("Persistence Error", e);
		}
	}

	@Override
	public TargetSystemEnviroment getInstallationTarget(String installationTarget) {
		List<TargetSystemEnviroment> targets = getInstallationTargets();
		List<TargetSystemEnviroment> result = targets.stream().filter(p -> p.getName().equals(installationTarget))
				.collect(Collectors.toList());
		Assert.isTrue(result.size() == 1, "Must be one , was: " + result.size());
		return result.get(0);
	}

	@Override
	public List<String> listAllFiles() {
		try {
			File[] listFiles = storagePath.getFile().listFiles();
			return Lists.newArrayList(listFiles).stream().map(f -> f.getName()).collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<String> listFiles(String prefix) {
		try {
			File[] listFiles = storagePath.getFile().listFiles();
			return Lists.newArrayList(listFiles).stream().filter(f -> f.getName().startsWith(prefix))
					.map(f -> f.getName()).collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void clean() {
		try {
			File parentDir = storagePath.getFile();
			FileUtils.cleanDirectory(parentDir);
		} catch (IOException e) {
			throw new RuntimeException(e);
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

	private File createDbModulesFile() throws IOException {
		File parentDir = storagePath.getFile();
		File revisions = new File(parentDir, DB_MODULES_JSON);
		return revisions;
	}

	private File createInstallationTargetFile() throws IOException {
		File parentDir = storagePath.getFile();
		File patchFile = new File(parentDir, INSTALLATION_TARGETS_JSON);
		return patchFile;
	}

	private File createServiceDataFile() throws IOException {
		File parentDir = storagePath.getFile();
		File patchFile = new File(parentDir, SERVICE_META_DATA_JSON);
		return patchFile;
	}

	private File createPatchFile(String patchNumber) throws IOException {
		File parentDir = storagePath.getFile();
		File patchFile = new File(parentDir, "Patch" + patchNumber + ".json");
		return patchFile;
	}

}
