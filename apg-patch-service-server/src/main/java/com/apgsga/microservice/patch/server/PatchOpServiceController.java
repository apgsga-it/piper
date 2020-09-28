package com.apgsga.microservice.patch.server;

import com.apgsga.microservice.patch.api.*;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Scope(org.springframework.web.context.WebApplicationContext.SCOPE_SESSION)
@RequestMapping(path = "patch/private")
public class PatchOpServiceController implements PatchOpService, PatchPersistence {

	protected static final Log LOGGER = LogFactory.getLog(PatchOpServiceController.class.getName());

	@Autowired
	private PatchPersistence repo;

	@Autowired
	@Qualifier("ServerBean")
	private PatchOpService patchService;

	@RequestMapping(value = "/findById/{id}", method = RequestMethod.GET)
	@ResponseBody
	@Override
	public Patch findById(@PathVariable("id") String patchNumber) {
		return repo.findById(patchNumber);
	}
	
	@RequestMapping(value = "/findPatchLogById/{id}", method = RequestMethod.GET)
	@ResponseBody
	@Override
	public PatchLog findPatchLogById(@PathVariable("id") String patchNumber) {
		return repo.findPatchLogById(patchNumber);
	}

	@RequestMapping(value = "/patchExists/{id}", method = RequestMethod.GET)
	@ResponseBody
	@Override
	public Boolean patchExists(@PathVariable("id") String patchNumber) {
		return repo.patchExists(patchNumber);

	}

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	@ResponseBody
	@Override
	public Patch save(@RequestBody Patch patch) {
		return patchService.save(patch);
	}
	
	@RequestMapping(value = "/savePatchLog", method = RequestMethod.POST)
	@ResponseBody
	@Override
	public void savePatchLog(@RequestBody String patchNumber) {
		repo.savePatchLog(patchNumber);
	}

	@RequestMapping(value = "/savePatch", method = RequestMethod.POST)
	@ResponseBody
	@Override
	public void savePatch(@RequestBody Patch patch) {
		repo.savePatch(patch);
	}

	@RequestMapping(value = "/findAllPatchIds", method = RequestMethod.GET)
	@ResponseBody
	@Override
	public List<String> findAllPatchIds() {
		return repo.findAllPatchIds();
	}

	@RequestMapping(value = "/removePatch", method = RequestMethod.POST)
	@Override
	public void removePatch(@RequestBody Patch patch) {
		repo.removePatch(patch);
	}

	@RequestMapping(value = "/saveDbModules", method = RequestMethod.POST)
	@Override
	public void saveDbModules(@RequestBody DbModules dbModules) {
		repo.saveDbModules(dbModules);
	}

	@RequestMapping(value = "/getDbModules", method = RequestMethod.GET)
	@ResponseBody
	@Override
	public DbModules getDbModules() {
		return repo.getDbModules();

	}

	@RequestMapping(value = "/saveServicesMetaData", method = RequestMethod.POST)
	@Override
	public void saveServicesMetaData(@RequestBody ServicesMetaData serviceData) {
		repo.saveServicesMetaData(serviceData);

	}

	@RequestMapping(value = "/getServicesMetaData", method = RequestMethod.GET)
	@ResponseBody
	@Override
	public ServicesMetaData getServicesMetaData() {
		return repo.getServicesMetaData();

	}

	@RequestMapping(value = "/executeStateChangeAction/{patchNumber}/{toStatus}", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@Override
	public void executeStateTransitionAction(@PathVariable("patchNumber") String patchNumber,
			@PathVariable("toStatus") String toStatus) {
		LOGGER.info("Got executeStateChangeAction Request for Patch: " + patchNumber + ", toState: " + toStatus);
		patchService.executeStateTransitionAction(patchNumber, toStatus);
		LOGGER.info(
				"Got executeStateChangeAction Request for Patch: " + patchNumber + ", toState: " + toStatus + " Done.");

	}

	@RequestMapping(value = "/listAllFiles", method = RequestMethod.GET)
	@ResponseBody
	@Override
	public List<String> listAllFiles() {
		return repo.listAllFiles();
	}

	@RequestMapping(value = "/listFiles/{prefix}", method = RequestMethod.GET)
	@ResponseBody
	@Override
	public List<String> listFiles(@PathVariable("prefix") String prefix) {
		return repo.listFiles(prefix);
	}

	@Override
	public ServiceMetaData findServiceByName(String serviceName) {
		throw new NotImplementedException();
	}

	@Override
	public void clean() {
		throw new UnsupportedOperationException();

	}

	@RequestMapping(value = "/cleanLocalMavenRepo", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@Override
	public void cleanLocalMavenRepo() {
		patchService.cleanLocalMavenRepo();
	}

	@RequestMapping(value = "/startAssembleAndDeployPipeline", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@Override
	public void startAssembleAndDeployPipeline(@RequestBody String target) {
		patchService.startAssembleAndDeployPipeline(target);
	}

	@RequestMapping(value = "/startInstallPipeline", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@Override
	public void startInstallPipeline(@RequestBody String target) {
		patchService.startInstallPipeline(target);
	}

	@RequestMapping(value = "/copyPatchFiles", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@Override
	public void copyPatchFiles(@RequestBody Map<String,String> params) {
		patchService.copyPatchFiles(params);
	}

	@RequestMapping(value = "/executeStateTransitionActionInDb/{patchNumber}/{statusNum}", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@Override
	public void executeStateTransitionActionInDb(@PathVariable("patchNumber") String patchNumber, @PathVariable("statusNum")Long statusNum) {
		patchService.executeStateTransitionActionInDb(patchNumber,statusNum);
	}

	@RequestMapping(value = "/patchIdsForStatus/{status}")
	@ResponseBody
	@Override
	public List<String> patchIdsForStatus(@PathVariable("status") String statusCode) {
		return patchService.patchIdsForStatus(statusCode);
	}

	@RequestMapping(value = "/startJenkinsJob/{jobName}", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@Override
	public void startJenkinsJob(@PathVariable String jobName) {
		patchService.startJenkinsJob(jobName);
	}

	@RequestMapping(value = "/startJenkinsJobWithParam/{jobName}", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@Override
	public void startJenkinsJob(@PathVariable String jobName, @RequestBody Map<String, String> jobParams) {
		patchService.startJenkinsJob(jobName,jobParams);
	}
}