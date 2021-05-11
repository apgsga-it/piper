package com.apgsga.microservice.patch.api;


import java.util.List;
import java.util.Map;

public interface PatchOpService {

		/*
		  TODO (jhe,2.12) Verfiy API Dependencies
		  extends PatchRdbms {
		 */

	/**
	 * Starts a build Pipeline with the given parameter
	 */
	void build(BuildParameter parameters);

	/**
	 * Trigger a piper internal process which will setup the pre-requisite for a patch to be build
	 */
	void setup(SetupParameter parameters);

	/**
	 * All changes on a patch Object need to be saved.
	 * 
	 * @param patch
	 *            a Patch Object
	 * @return MicroservicePatch with Server added data
	 */
	Patch save(Patch patch);

	/**
	 * Save patch information in a PatchLog<patchNumber>.log file
	 * @param patchNumber  the Patch number
	 * @param logDetails Details of a Log
	 */
	void savePatchLog(String patchNumber, PatchLogDetails logDetails);

	/**
	 * Clean local Mavenrepo
	 * 
	 */
	void cleanLocalMavenRepo();

	/**
	 * Dynamically create an assembleAndDeploy Pipeline, and start it
	 */
	void startAssembleAndDeployPipeline(AssembleAndDeployParameters parameters);

	/**
	 * Dynamically create an install Pipeline, and start it
	 */
	void startInstallPipeline(InstallParameters parameters);

	List<String> patchIdsForStatus(String statusCode);

	void notify(NotificationParameters params);

	void startOnDemandPipeline(OnDemandParameter parameters);

	void startOnClonePipeline(OnCloneParameters parameters);

	void checkPatchConflicts(List<PatchListParameter> parameters);

}
