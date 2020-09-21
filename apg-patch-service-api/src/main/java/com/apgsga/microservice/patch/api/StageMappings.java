package com.apgsga.microservice.patch.api;

import java.util.List;

public class StageMappings {

    private List<StageMapping> stageMappings;

    public List<StageMapping> getStageMappings() {
        return stageMappings;
    }

    public void setStageMappings(List<StageMapping> stageMappings) {
        this.stageMappings = stageMappings;
    }
}
