package com.apgsga.microservice.patch.api;


import java.util.List;
import java.util.Map;

public interface PatchOpService {

		/**
		 * TODO (jhe,2.12) Verfiy API Dependencies
		 * extends PatchRdbms {
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
	 * Starts the corresponding "install" pipeline
	 * @param target target for which will assemble and deploy (chei211,chti211,etc...)
	 */
	void startInstallPipeline(String target);

	/**
	 * Copies JSON Patch files to a destination folder
	 * @param params : 2 parameters required with following keys: "status" and "destFolder"
	 */
	//TODO JHE (18.11.2020): will probably be removed, but I want to wait until implemenation of assembleAndDeploy to be 100% sure
	@Deprecated
	void copyPatchFiles(Map<String,String> params);

	List<String> patchIdsForStatus(String statusCode);

	void notify(NotificationParameters params);


}
