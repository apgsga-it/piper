package com.apgsga.microservice.patch.server.impl.vcs;

import java.util.List;

public interface VcsCommandSession {
	
	public void connect();
	public void disconnect();

	public List<String> execCommand(String[] command);
}
