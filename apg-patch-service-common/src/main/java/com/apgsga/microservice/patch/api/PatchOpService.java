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
	 * This method , is intended to be integrated into the "Clone" Process of
	 * Apg. And triggers resp. implements all for the Patch Service relevant
	 * functionality, which needs to be done after a target has been cloned.
	 * 
	 * @param clonedTarget the Id of the cloned System
	 */
	public void onCloneOf(String clonedTarget);
}
