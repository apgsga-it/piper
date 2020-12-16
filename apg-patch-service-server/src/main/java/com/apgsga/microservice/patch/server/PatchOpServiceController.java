package com.apgsga.microservice.patch.server;

import com.apgsga.microservice.patch.api.Package;
import com.apgsga.microservice.patch.api.*;
import com.apgsga.patch.db.integration.api.PatchRdbms;
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

	@Autowired
	private PatchRdbms patchRdbms;

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
	
	@RequestMapping(value = "/savePatchLog/{patchNumber}", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@Override
	public void savePatchLog(@PathVariable String patchNumber, @RequestBody PatchLogDetails logDetails) {
		repo.savePatchLog(patchNumber,logDetails);
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

	@Override
	public ServiceMetaData getServiceMetaDataByName(String serviceName) {
		// TODO (che, 9.12)
		return null;
	}

	@RequestMapping(value = "/getServicesMetaData", method = RequestMethod.GET)
	@ResponseBody
	@Override
	public ServicesMetaData getServicesMetaData() {
		return repo.getServicesMetaData();

	}

	@RequestMapping(value = "/build", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@Override
	public void build(@RequestBody BuildParameter buildParams) {
		LOGGER.info("Got Build Request for " + buildParams.toString());
		patchService.build(buildParams);
	}

	@RequestMapping(value = "/setup", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@Override
	public void setup(@RequestBody SetupParameter setupParams) {
		LOGGER.info("Got setup request for " + setupParams.toString());
		patchService.setup(setupParams);
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
	public OnDemandTargets onDemandTargets() {
		// TODO (che, 9.12)
		return null;
	}

	@Override
	public StageMappings stageMappings() {
		// TODO (che, 9.12)
		return null;
	}

	@Override
	public TargetInstances targetInstances() {
		// TODO (che, 9.12)
		return null;
	}

	@Override
	public List<Package> packagesFor(Service service) {
		// TODO (che, 9.12)
		return null;
	}

	@Override
	public String targetFor(String stageName) {
		// TODO (che, 9.12)
		return null;
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
	public void startAssembleAndDeployPipeline(@RequestBody AssembleAndDeployParameters parameters) {
		patchService.startAssembleAndDeployPipeline(parameters);
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

	@RequestMapping(value = "/notify", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public void notify(@RequestBody NotificationParameters params) {
		patchRdbms.notify(params);
	}

	@RequestMapping(value = "/patchIdsForStatus/{status}")
	@ResponseBody
	public List<String> patchIdsForStatus(@PathVariable("status") String statusCode) {
		return patchRdbms.patchIdsForStatus(statusCode);
	}



}