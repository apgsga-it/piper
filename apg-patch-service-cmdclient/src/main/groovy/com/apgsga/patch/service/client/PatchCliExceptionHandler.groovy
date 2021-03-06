package com.apgsga.patch.service.client

import com.apgsga.microservice.patch.api.PatchErrorMessage
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.HttpMessageConverterExtractor
import org.springframework.web.client.ResponseErrorHandler

class PatchCliExceptionHandler implements ResponseErrorHandler {

	private static List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>()
	
	static {
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
		converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
		messageConverters.add(converter)
	}

	@Override
	boolean hasError(ClientHttpResponse response) throws IOException {
		return hasError(response.getStatusCode())
	}

	protected  static boolean hasError(HttpStatus statusCode) {
		return (statusCode.is4xxClientError() || statusCode.is5xxServerError())
	}

    @SuppressWarnings('GroovyUncheckedAssignmentOfMemberOfRawType')
    @Override
    void handleError(ClientHttpResponse response) throws IOException {
		HttpMessageConverterExtractor<PatchErrorMessage> errorMessageExtractor =
				new HttpMessageConverterExtractor(PatchErrorMessage.class, messageConverters)
		PatchErrorMessage errorObject = errorMessageExtractor.extractData(response)
		throw new PatchClientServerException(errorObject)
	}
}
