package com.apgsga.microservice.patch.api;

import java.util.Map;

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
	 */
	void restartProdPipeline(String patchNumber);

	/**
	 * All changes on a patch Object need to be saved.
	 * 
	 * @param patch
	 *            a Patch Object
	 * @return MicroservicePatch with Server added data
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

	/**
	 * Start the corresponding "assemble and deploy" pipeline
	 * @param params map with following keys: target and tmpfolder
	 */
	void startAssembleAndDeployPipeline(Map<String,String> params);
}
