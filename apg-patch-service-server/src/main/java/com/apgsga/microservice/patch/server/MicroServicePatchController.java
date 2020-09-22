package com.apgsga.microservice.patch.server;

import java.util.List;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.apgsga.microservice.patch.api.DbObject;
import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchLog;
import com.apgsga.microservice.patch.api.PatchService;
import com.apgsga.microservice.patch.api.SearchCondition;
import com.apgsga.microservice.patch.api.ServiceMetaData;

@RestController
@Scope(org.springframework.web.context.WebApplicationContext.SCOPE_SESSION)
@RequestMapping(path = "patch/public")
public class MicroServicePatchController implements PatchService {

	protected final Log LOGGER = LogFactory.getLog(getClass());

	@Autowired
	@Qualifier("ServerBean")
	private PatchService patchService;

	@RequestMapping(value = "/findById/{id}", method = RequestMethod.GET)
	@ResponseBody
	@Override
	public Patch findById(@PathVariable("id") String patchNummer) {
		return patchService.findById(patchNummer);
	}

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	@ResponseBody
	@Override
	public Patch save(@RequestBody Patch patch) {
		return patchService.save(patch);
	}
	
	@RequestMapping(value = "/log", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@Override
	public void log(@RequestBody Patch patch) {
  		patchService.log(patch);
	}

	@RequestMapping(value = "/remove", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@Override
	public void remove(@RequestBody Patch patch) {
		patchService.remove(patch);
	}

	@RequestMapping(value = "/listDbModules", method = RequestMethod.GET)
	@ResponseBody
	@Override
	public List<String> listDbModules() {
		return patchService.listDbModules();
	}

	@RequestMapping(value = "/listDbObjectsChanged/{id}/{search}", method = RequestMethod.GET)
	@ResponseBody
	@Override
	public List<DbObject> listAllObjectsChangedForDbModule(@PathVariable("id") String patchId,
			@PathVariable("search") String searchString) {
		return patchService.listAllObjectsChangedForDbModule(patchId, searchString);
	}

	@RequestMapping(value = "/listAllDbObjects/{id}/{search}", method = RequestMethod.GET)
	@ResponseBody
	@Override
	public List<DbObject> listAllObjectsForDbModule(@PathVariable("id") String patchNumber, @PathVariable("search") String searchString) {
		return patchService.listAllObjectsForDbModule(patchNumber,searchString);
	}

	@RequestMapping(value = "/listAllDbObjectsForUser/{id}/{search}/{username}", method = RequestMethod.GET)
	@ResponseBody
	@Override
	public List<DbObject> listAllObjectsForDbModule(@PathVariable("id") String patchNumber, @PathVariable("search") String searchString, @PathVariable("username") String username) {
		return patchService.listAllObjectsForDbModule(patchNumber,searchString,username);
	}

	@RequestMapping(value = "/listMavenArtifacts/{serviceName}", method = RequestMethod.GET)
	@ResponseBody
	public List<MavenArtifact> listMavenArtifacts(@PathVariable("serviceName") String serviceName) {
		return patchService.listMavenArtifacts(serviceName);
	}

	@RequestMapping(value = "/listMavenArtifactsWithFilter/{serviceName}/{searchCondition}", method = RequestMethod.GET)
	@ResponseBody
	public List<MavenArtifact> listMavenArtifacts(@PathVariable("serviceName") String serviceName,
			@PathVariable("searchCondition") String searchCondition) {
		return patchService.listMavenArtifacts(serviceName, SearchCondition.forValue(searchCondition));
	}

	@Override
	public List<MavenArtifact> listMavenArtifacts(String serviceName, SearchCondition filter) {
		return patchService.listMavenArtifacts(serviceName, filter);

	}


	@RequestMapping(value = "/listServiceData", method = RequestMethod.GET)
	@ResponseBody
	@Override
	public List<ServiceMetaData> listServiceData() {
		return patchService.listServiceData();
	}

	@RequestMapping(value = "/listInstallationTargets", method = RequestMethod.GET)
	@ResponseBody
	@Override
	public List<String> listOnDemandTargets() {
		return patchService.listOnDemandTargets();
	}

	@RequestMapping(value = "/findByIds", method = RequestMethod.POST)
	@ResponseBody
	@Override
	public List<Patch> findByIds(@RequestBody List<String> patchIds) {
		return patchService.findByIds(patchIds);
	}

	@RequestMapping(value = "/findPatchLogById/{id}", method = RequestMethod.GET)
	@ResponseBody
	@Override
	public PatchLog findPatchLogById(@PathVariable("id") String patchNummer) {
		return patchService.findPatchLogById(patchNummer);
	}

	@RequestMapping(value = "/findWithObjectName", method = RequestMethod.POST)
	@ResponseBody
	@Override
	public List<Patch> findWithObjectName(@RequestBody String objectName) {
		return patchService.findWithObjectName(objectName);
	}
}
