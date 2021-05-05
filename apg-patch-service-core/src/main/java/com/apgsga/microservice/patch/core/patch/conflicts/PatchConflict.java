package com.apgsga.microservice.patch.core.patch.conflicts;

import com.apgsga.microservice.patch.api.DbObject;
import com.apgsga.microservice.patch.api.MavenArtifact;
import com.apgsga.microservice.patch.api.Patch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;

import java.util.List;
import java.util.Map;

//TODO JHE : better use Lombok here

@Getter
public class PatchConflict {

    private Patch p1;
    private Patch p2;
    private Map<String,List<MavenArtifact>> serviceWithMavenArtifacts;
    private List<DbObject> dbObjects;
    private List<String> dockerServices;

    private PatchConflict(){
        serviceWithMavenArtifacts = Maps.newHashMap();
        dbObjects = Lists.newArrayList();
        dockerServices = Lists.newArrayList();
    }

    public static PatchConflict create() {
        return new PatchConflict();
    }

    protected PatchConflict patch1(Patch patch) {
        p1 = patch;
        return this;
    }

    protected PatchConflict patch2(Patch patch) {
        p2 = patch;
        return this;
    }

    protected PatchConflict mavenArtifacts(Map<String,List<MavenArtifact>> mavenArtifactsForServices) {
        this.serviceWithMavenArtifacts = mavenArtifactsForServices;
        return this;
    }

    protected PatchConflict dbObjects(List<DbObject> dbObjects) {
        this.dbObjects = dbObjects;
        return this;
    }

    protected PatchConflict dockerServices(List<String> dockerServices) {
        this.dockerServices = dockerServices;
        return this;
    }
}
