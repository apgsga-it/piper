package com.apgsga.microservice.patch.api;

public interface PatchOpService {

	/**
	 * Execute a Action, which leads when successful to the toStatus
	 * 
	 * @param patchNumber
	 * @param toStatus
	 */
	void executeStateTransitionAction(String patchNumber, String toStatus);
	
	/**
	 * Execute a Action, which leads when successful to the toStatus
	 * 
	 * @param patchNumber
	 * @param toStatus
	 */
	void restartProdPipeline(String patchNumber);

	/**
	 * All changes on a patch Object need to be saved.
	 * 
	 * @param patch
	 *            a Patch Object
	 * @return MicroservicePatch with Server added data
	 * @throws PatchContainerException
	 */
	Patch save(Patch patch);

	/**
	 * Clone a specific target
	 * 
	 * @param source
	 * @param target
	 *             eg.: CHPI211,CHEI212
	 */
	void onClone(String source, String target);

	/**
	 * Clean local Mavenrepo
	 * 
	 */
	void cleanLocalMavenRepo();
}
