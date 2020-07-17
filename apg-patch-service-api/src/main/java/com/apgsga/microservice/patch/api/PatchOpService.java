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
	 * All changes on a patch Object need to be saved.
	 * 
	 * @param patch
	 *            a Patch Object
	 * @return MicroservicePatch with Server added data
	 */
	Patch save(Patch patch);

	/**
	 * Clean local Mavenrepo
	 * 
	 */
	void cleanLocalMavenRepo();

	/**
	 * Starts the corresponding "assemble and deploy" pipeline
	 * @param target target for which will assemble and deploy (chei211,chti211,etc...)
	 */
	void startAssembleAndDeployPipeline(String target);

	/**
	 * Starts the corresponding "install" pipeline
	 * @param target target for which will assemble and deploy (chei211,chti211,etc...)
	 */
	void startInstallPipeline(String target);
}
