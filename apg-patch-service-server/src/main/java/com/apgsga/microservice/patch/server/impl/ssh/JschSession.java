package com.apgsga.microservice.patch.server.impl.ssh;

import java.util.List;

public interface JschSession {
	
	public void connect();
	public void disconnect();

	public List<String> execCommand(String command);
}
