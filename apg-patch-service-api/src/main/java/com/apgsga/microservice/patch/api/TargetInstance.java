package com.apgsga.microservice.patch.api;

import java.util.List;

public class TargetInstance {

    private String name;
    private List<ServiceMetaData> services;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ServiceMetaData> getServices() {
        return services;
    }

    public void setServices(List<ServiceMetaData> services) {
        this.services = services;
    }
}
