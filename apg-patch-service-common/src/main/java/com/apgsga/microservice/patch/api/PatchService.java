package com.apgsga.microservice.patch.api;

import java.util.List;

/**
 * @author che
 *
 */
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
	 * @param patch Patch, for which Artifacts are listed
	 * @param filter Filter, which should be applied for search
	 * @return list of Maven Arifacts relevant for Patch
	 */
	// TODO (che, 30.10) this really should'nt be dependent of the Patch
	public List<MavenArtifact> listMavenArtifacts(Patch patch, SearchCondition filter); 
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
	 * Retrieves a PatchLog by Id
	 * @param patchNummer
	 * @return a PatchLog Object
	 */
	public PatchLog findPatchLogById(String patchNummer);
	
	public List<Patch> findByIds(List<String> patchIds);	
	/**
	 * All changes on a patch Object need to be saved.
	 * @param patch a Patch Object
	 * @return MicroservicePatch with Server added data
	 * @throws PatchContainerException
	 */
	
	public Patch save(Patch patch);
	
	/**
	 * Log all steps done for a patch
	 * @param patch , Patch where to get the information from
	 */
	public void log(Patch patch);

	/**
	 * A Patch object is removed from the PatchContainer
	 * @param patch a Patch object
	 * @throws PatchContainerException
	 */
	public void remove(Patch patch);
	
	/**
	 * Search for patches which contains given object name
	 * @param objectName name of searched object
	 * @return List of patches containing Object with given name
	 */
	public List<Patch> findWithObjectName(String objectName);

}
