package com.apgsga.microservice.patch.api;

public interface PatchOpService {

	/**
	 * Execute a Action, which leads when successful to the toStatus
	 * 
	 * @param patchNumber
	 * @param toStatus
	 */
	public void executeStateTransitionAction(String patchNumber, String toStatus);
	
	/**
	 * Execute a Action, which leads when successful to the toStatus
	 * 
	 * @param patchNumber
	 * @param toStatus
	 */
	public void restartProdPipeline(String patchNumber);

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
	 * Clone a specific target
	 * 
	 * @param source
	 * @param target
	 *             eg.: CHPI211,CHEI212
	 */
	public void onClone(String source,String target);

	/**
	 * Clean local Mavenrepo
	 * 
	 */
	public void cleanLocalMavenRepo();
}
