package com.apgsga.microservice.patch.server;

import com.apgsga.microservice.patch.api.Patch;
import com.apgsga.microservice.patch.api.PatchOpService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@Scope(org.springframework.web.context.WebApplicationContext.SCOPE_SESSION)
@RequestMapping(path = "patch/private")
public class PatchOpServiceController implements PatchOpService {

	protected static final Log LOGGER = LogFactory.getLog(PatchOpServiceController.class.getName());

	@Autowired
	@Qualifier("ServerBean")
	private PatchOpService patchService;

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	@ResponseBody
	@Override
	public Patch save(@RequestBody Patch patch) {
		return patchService.save(patch);
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

	@RequestMapping(value = "/onClone", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@Override
	public void onClone(@RequestParam("source") String source, @RequestParam("target") String target) {
		patchService.onClone(source,target);
	}

	@RequestMapping(value = "/cleanLocalMavenRepo", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@Override
	public void cleanLocalMavenRepo() {
		patchService.cleanLocalMavenRepo();
	}
}