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
	
	/**
	 * Aggregate a list of patches into one single big patch
	 * 
	 * @param patchList : comma separated list of patches to be aggregated
	 */
	// JHE (04.04.2019): to be discussed, is that really part of this API ?
	public void aggregatePatches(String patchList);
}
