package com.apgsga.microservice.patch.server;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.apgsga.microservice.patch.api.DbModules;
import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchOpService;
import com.apgsga.microservice.patch.api.PatchPersistence;
import com.apgsga.microservice.patch.api.ServiceMetaData;
import com.apgsga.microservice.patch.api.ServicesMetaData;

@RestController
@Scope(org.springframework.web.context.WebApplicationContext.SCOPE_SESSION)
@RequestMapping(path = "patch/private")
public class PatchOpServiceController implements PatchOpService, PatchPersistence {

	static protected final Log LOGGER = LogFactory.getLog(PatchOpServiceController.class.getName());

	@Autowired
	private PatchPersistence repo;

	@Autowired
	@Qualifier("ServerBean")
	private PatchOpService patchService;

	@RequestMapping(value = "/findById/{id}", method = RequestMethod.GET)
	@ResponseBody
	@Override
	public Patch findById(@PathVariable("id") String patchNummer) {
		return repo.findById(patchNummer);
	}

	@RequestMapping(value = "/patchExists/{id}", method = RequestMethod.GET)
	@ResponseBody
	@Override
	public Boolean patchExists(@PathVariable("id") String patchNummber) {
		return repo.patchExists(patchNummber);

	}

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	@ResponseBody
	@Override
	public Patch save(@RequestBody Patch patch) {
		return patchService.save(patch);
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

	@Override
	public void init() throws IOException {
		throw new UnsupportedOperationException();
	}

	
	@RequestMapping(value = "/onClone", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@Override
	public void onClone(@RequestParam("target") String target) {
		patchService.onClone(target);
	}
	
	@RequestMapping(value = "/cleanLocalMavenRepo", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@Override
	public void cleanLocalMavenRepo() {
		patchService.cleanLocalMavenRepo();
	}
}
