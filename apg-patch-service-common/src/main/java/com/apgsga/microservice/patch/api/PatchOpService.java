package com.apgsga.microservice.patch.api;

import java.util.List;

public interface PatchOpService {

	/**
	 * Execute a Action, which leads when successful to the toStatus
	 * 
	 * @param patchNumber
	 * @param toStatus
	 */
	public void executeStateTransitionAction(String patchNumber, String toStatus);

	/**
	 * All changes on a patch Object need to be saved.
	 * 
	 * @param patch
	 *            a Patch Object
	 * @return MicroservicePatch with Server added data
	 * @throws PatchContainerException
	 */
	public Patch save(Patch patch);

	/**
	 * Validate that name of Maven Artifacts match CVS directories
	 * 
	 * @param: version
	 *             bom Version cvsBranch branch where module have been
	 *             checked-in
	 * @return: List of MavenArtifact for Artifacts having a wrong name
	 */
	public List<MavenArtifact> invalidArtifactNames(String version, String cvsBranch);
	
	/**
	 * Clone a specific target
	 * 
	 * @param target
	 * 			eg.: CHEI212
	 */
	public void onClone(String target);
}
