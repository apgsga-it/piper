package com.apgsga.microservice.patch.api;

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



}
