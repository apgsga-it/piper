package com.apgsga.microservice.patch.api;

import java.util.List;

/**
 * @author che
 *
 */
public interface PatchService {

	
	
	/**
	 * Start the Jenkins Install Pipeline for a Patch. 
	 * @param patch the Patch, which contains the Target to Install
	 */
	public void startInstallPipeline(Patch patch); 
	
	
	/**
	 * @return the Configuration Data of the known Target Services
	 */
	public List<ServiceMetaData> listServiceData(); 
	
	
	/**
	 * @param masterTarget the System Target the Patch System is requested from
	 * @return List for Installation Targets for the Requesting TargetSystem
	 */
	public List<String> listInstallationTargetsFor(String masterTarget);
	
	/**
	 * List all known DbModules
	 * 
	 * @return List of of Db Modules
	 */
	public List<String> listDbModules(); 
	
	
	/**
	 * List all Maven Artifacts 
	 * @return list of Maven Arifacts
	 */
	public List<MavenArtifact> listMavenArtifacts(Patch patch);
	/**
	 * List all changed 
	 * @param searchString
	 * @return List of changed DbObjects 
	 */
	public List<DbObject> listAllObjectsChangedForDbModule(String patchNummber,String searchString); 
	

	/**
	 * Retrieves a Patch by Id. 
	 * @param patchId the Identifier of the Patch
	 * @return a Patch Object
	 * @throws PatchContainerException
	 */
	public Patch findById(String patchNummer);
	
	/**
	 * All changes on a patch Object need to be saved.
	 * @param patch a Patch Object
	 * @return MicroservicePatch with Server added data
	 * @throws PatchContainerException
	 */
	public Patch save(Patch patch);

	/**
	 * A Patch object is removed from the PatchContainer
	 * @param patch a Patch object
	 * @throws PatchContainerException
	 */
	public void remove(Patch patch);
	

}
