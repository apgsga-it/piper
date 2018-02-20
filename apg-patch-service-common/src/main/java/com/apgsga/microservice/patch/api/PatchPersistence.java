package com.apgsga.microservice.patch.api;

import java.io.IOException;
import java.util.List;

public interface PatchPersistence {

	Patch findById(String patchNummer);

	Boolean patchExists(String patchNummber);

	public List<String> findAllPatchIds();

	public void save(Patch patch);

	public void remove(Patch patch);

	public void save(DbModules dbModules);

	public DbModules getDbModules();

	public void saveServicesMetaData(ServicesMetaData serviceData);

	public ServicesMetaData getServicesMetaData();

	public void saveTargetSystemEnviroments(List<TargetSystemEnviroment> installationTargets);

	public List<TargetSystemEnviroment> getInstallationTargets();

	public TargetSystemEnviroment getInstallationTarget(String installationTarget);

	public ServiceMetaData findServiceByName(String serviceName);

	public void clean();

	public void init() throws IOException;

}
