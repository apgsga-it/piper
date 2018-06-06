package com.apgsga.patch.service.client

import com.apgsga.microservice.patch.api.PatchErrorMessage

class PatchClientServerException extends Throwable {
	PatchErrorMessage errorMessage;

	public PatchClientServerException(PatchErrorMessage errorMessage) {
		this.errorMessage = errorMessage;
	} 

	public PatchErrorMessage getErrorMessage() {
		return errorMessage;
	}
	

}
