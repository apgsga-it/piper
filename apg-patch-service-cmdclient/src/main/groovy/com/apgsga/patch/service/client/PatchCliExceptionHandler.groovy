package com.apgsga.patch.service.client

import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.HttpMessageConverterExtractor
import org.springframework.web.client.ResponseErrorHandler

import com.apgsga.microservice.patch.api.PatchErrorMessage

class PatchCliExceptionHandler implements ResponseErrorHandler {

	private static List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
	
	static {
		messageConverters.add(new MappingJackson2HttpMessageConverter())
	}

	@Override
	public boolean hasError(ClientHttpResponse response) throws IOException {
		return hasError(response.getStatusCode());
	}

	protected boolean hasError(HttpStatus statusCode) {
		return (statusCode.is4xxClientError() || statusCode.is5xxServerError());
	}

	@Override
	public void handleError(ClientHttpResponse response) throws IOException {
		HttpMessageConverterExtractor<PatchErrorMessage> errorMessageExtractor =
				new HttpMessageConverterExtractor(PatchErrorMessage.class, messageConverters);
		PatchErrorMessage errorObject = errorMessageExtractor.extractData(response);
		throw new PatchClientServerException(errorObject);
	}
}
