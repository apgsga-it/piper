package com.apgsga.microservice.patch.api;

import java.util.List;
import java.util.Map;

public interface PatchOpService  {

	/**
	 * Execute a Action, which leads when successful to the toStatus
	 * 
	 * @param patchNumber
	 * @param toStatus
	 */
	public void executeStateTransitionAction(String patchNumber, String toStatus);
	
	
	/**
	 * All changes on a patch Object need to be saved.
	 * @param patch a Patch Object
	 * @return MicroservicePatch with Server added data
	 * @throws PatchContainerException
	 */
	public Patch save(Patch patch);

	/**
	 * Validate that name of Maven Artifacts match CVS directories
	 * @param: version bom Version
	 * @return: map with List of MavenArtifact for each "mistakes"
	 */
	public Map<String,List<MavenArtifact>> invalidArtifactNames(String version);
	
	/**
	 * Validate that name of Maven Artifacts match CVS directories for modules within the Patch
	 * @param: patch a Patch Object
	 * @return: map with List of MavenArtifact for each "mistakes"
	 */
//	public Map<String,List<MavenArtifact>> invalidArtifactNames(Patch patch);

}
