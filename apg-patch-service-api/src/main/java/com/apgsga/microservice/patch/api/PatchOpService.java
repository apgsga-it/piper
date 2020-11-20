package com.apgsga.microservice.patch.api;

import com.apgsga.patch.db.integration.api.PatchRdbms;

import java.util.Map;

public interface PatchOpService extends PatchRdbms {

	/**
	 * Starts a build Pipeline for the given patch. Notify back the db with the successsNotification
	 * Parameter example: 8000,Informatiktest,InformatiktestBuildOk
	 */
	void build(String patchNumber, String stage, String successNotification);

	/**
	 * Trigger a piper internal process which will setup the pre-requisite for a patch to be build
	 * @param patchNumber
	 * @param successNotification
	 */
	void setup(String patchNumber, String successNotification);

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
	 * @param logDetails
	 */
	void savePatchLog(String patchNumber, PatchLogDetails logDetails);

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

	/**
	 * Copies JSON Patch files to a destination folder
	 * @param params : 2 parameters required with following keys: "status" and "destFolder"
	 */
	//TODO JHE (18.11.2020): will probably be removed, but I want to wait until implemenation of assembleAndDeploy to be 100% sure
	@Deprecated
	void copyPatchFiles(Map<String,String> params);


}
