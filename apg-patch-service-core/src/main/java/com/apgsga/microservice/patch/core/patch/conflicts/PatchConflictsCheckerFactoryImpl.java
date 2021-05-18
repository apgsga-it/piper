package com.apgsga.microservice.patch.core.patch.conflicts;

public class PatchConflictsCheckerFactoryImpl implements PatchConflictCheckerFactory {

    @Override
    public PatchConflictsChecker create() {
        return PatchConflictsCheckerImpl.create();
    }
}
