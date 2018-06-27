package com.apgsga.microservice.patch.server.impl.jenkins;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JenkinsAdminMockClient implements JenkinsAdminClient {

	protected final Log LOGGER = LogFactory.getLog(getClass());
	
	@Override
	public void onClone(String target) {
		LOGGER.info("onClone for " + target);		
	}

}
