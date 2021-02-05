package com.apgsga.microservice.patch.core.impl.jenkins;

public class PackagerInfo {
    public String name;
    public String targetHost;
    public String vcsBranch;

    public PackagerInfo(String name, String targetHost, String vcsBranch) {
        this.name = name;
        this.targetHost = targetHost;
        this.vcsBranch = vcsBranch;
    }
}
