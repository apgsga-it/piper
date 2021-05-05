package com.apgsga.microservice.patch.core.patch.conflicts;

import com.apgsga.microservice.patch.api.Patch;

import java.util.List;

public interface PatchConflictsChecker {

    void addPatch(Patch p);

    List<PatchConflict> checkConflicts();
}
