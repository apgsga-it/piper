package com.apgsga.microservice.patch.api;

import com.apgsga.patch.db.integration.api.PatchRdbms;

import java.io.File;
import java.util.Map;

public interface PatchOpService extends PatchRdbms {

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
	 * Save patch information in a PatchLog<patchNumber>.log file
	 * @param patchNumber
	 */
	void savePatchLog(String patchNumber);

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
	 * Starts the corresponding build (or compile) pipeline
	 * @param patchNumber eg.: 3456
	 */
	void startJenkinsBuildPipeline(String patchNumber);

	/**
	 * Copies JSON Patch files to a destination folder
	 * @param params : 2 parameters required with following keys: "status" and "destFolder"
	 */
	void copyPatchFiles(Map<String,String> params);

	/**
	 * Starts a Jenkins Job
	 * @param jobName
	 */
	void startJenkinsJob(String jobName);

	/**
	 *
	 * @param jobName
	 * @param params Standard key=value parameters Map
	 *
	 */
	void startJenkinsJob(String jobName, Map<String,String> params);
}
