package com.apgsga.microservice.patch.api;

import java.util.List;

/**
 * @author che
 *
 */
public interface PatchService {

	/**
	 * @return the Configuration Data of the known Target Services
	 */
    List<ServiceMetaData> listServiceData();
	
	
	/**
	 * @return List of all targets installable via onDemand
	 */
    List<String> listOnDemandTargets();
	
	/**
	 * List all known DbModules
	 * 
	 * @return List of of Db Modules
	 */
    List<String> listDbModules();
	
	/**
	 * @param serviceName Patch, for which Artifacts are listed
	 * @param filter Filter, which should be applied for search
	 * @return list of Maven Arifacts relevant for Patch
	 */
    List<MavenArtifact> listMavenArtifacts(String serviceName, SearchCondition filter);
	/**
	 * List all Maven Artifacts 
	 * @return list of Maven Arifacts
	 * @param serviceName the Name of the Service for which the Artifacts are listed
	 */
    List<MavenArtifact> listMavenArtifacts(String serviceName);
	/**
	 * List all changed 
	 * @param searchString is contained in Filename
	 * @return List of changed DbObjects 
	 */
    List<DbObject> listAllObjectsChangedForDbModule(String patchNumber, String searchString);

	/**
	 * List all SQL Resource for module name matching searchString
	 * @param patchNumber of the Patch
	 * @param searchString  is contained in Filename
	 * @return List of DbObjects
	 */
    List<DbObject> listAllObjectsForDbModule(String patchNumber, String searchString);

	/**
	 * List all SQL Resource for module name matching searchString. Temporary checkout will be done in a folder specific for the given username
	 * @param patchNumber of the Patch
	 * @param searchString  is contained in Filename
	 * @param username the User, who has commited the changes
	 * @return List of DbObjects
	 */
    List<DbObject> listAllObjectsForDbModule(String patchNumber, String searchString, String username);

	/**
	 * Retrieves a Patch by Id. 
	 * @param patchNummer the Identifier of the Patch
	 * @return a Patch Object
	 */
    Patch findById(String patchNummer);
	
	/**
	 * Retrieves a PatchLog by Id
	 * @param patchNummer
	 * @return a PatchLog Object
	 */
    PatchLog findPatchLogById(String patchNummer);
	
	List<Patch> findByIds(List<String> patchIds);
	/**
	 * All changes on a patch Object need to be saved.
	 * @param patch a Patch Object
	 * @return MicroservicePatch with Server added data
	 */

    Patch save(Patch patch);
	
	/**
	 * Log all steps done for a patch
	 * @param logDetails the Log Details of a Patchlog
	 */
    void log(String patchNumber, PatchLogDetails logDetails);

	/**
	 * A Patch object is removed from the PatchContainer
	 * @param patch a Patch object
	 */
    void remove(Patch patch);
	
	/**
	 * Search for patches which contains given object name
	 * @param objectName name of searched object
	 * @return List of patches containing Object with given name
	 */
    List<Patch> findWithObjectName(String objectName);

}
