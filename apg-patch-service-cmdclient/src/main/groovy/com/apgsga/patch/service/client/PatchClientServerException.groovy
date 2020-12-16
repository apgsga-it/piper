package com.apgsga.patch.service.client

import com.apgsga.microservice.patch.api.PatchErrorMessage

class PatchClientServerException extends Throwable {
	PatchErrorMessage errorMessage

	PatchClientServerException(PatchErrorMessage errorMessage) {
		this.errorMessage = errorMessage
	}

	PatchErrorMessage getErrorMessage() {
		return errorMessage
	}
	

}
