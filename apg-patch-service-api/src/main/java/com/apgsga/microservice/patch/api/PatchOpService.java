package com.apgsga.microservice.patch.api;

public interface PatchOpService {

	/**
	 * Execute a Action, which leads when successful to the toStatus
	 * 
	 * @param patchNumber Id for a Patch
	 * @param toStatus To State in terms of Apg Patchworkflow
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
	 * Clone a specific target
	 * 
	 * @param source  Target from which is cloned
	 * @param target Target to which is cloned
	 *             eg.: CHPI211,CHEI212
	 */
	void onClone(String source, String target);

	/**
	 * Clean local Mavenrepo
	 * 
	 */
	void cleanLocalMavenRepo();
}
