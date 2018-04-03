package com.apgsga.microservice.patch.server.impl.vcs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JschLoggingMockFactory implements JschSessionFactory {
	
	protected final Log LOGGER = LogFactory.getLog(getClass());


	@Override
	public VcsCommandSession create() {
		LOGGER.info("Create Mock Session");
		return new JschLoggingMockSession(); 
	}

}
