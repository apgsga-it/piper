package com.apgsga.patch.service.client.serverless

import com.apgsga.microservice.patch.api.Patch
import com.apgsga.microservice.patch.api.PatchOpService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class PatchServerlessImpl implements PatchOpService {

	@Autowired
	@Qualifier("ServerBean")
	private PatchOpService patchOpService


	PatchServerlessImpl() {
	}


	@Override
	void executeStateTransitionAction(String patchNumber, String toStatus) {
		patchOpService.executeStateTransitionAction(patchNumber,toStatus)
	}
	@Override
	void cleanLocalMavenRepo() {
		patchOpService.cleanLocalMavenRepo()
	}

	@Override
	Patch save(Patch patch) {
		patchOpService.save(patch)
	}


	@Override
	void onClone(String source, String target) {
		patchOpService.onClone(source,target)
	}

}