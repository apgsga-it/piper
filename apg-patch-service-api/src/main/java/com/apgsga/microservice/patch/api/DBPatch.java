package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@JsonDeserialize(builder = DBPatch.DBPatchBuilder.class)
@Builder(toBuilder = true)
@EqualsAndHashCode(exclude = {"dbObjects"})
public class DBPatch {

    @Getter
    String dbPatchBranch;
    @Getter
    String prodBranch;
    @Getter
    String patchTag;
    @Getter
    @Builder.Default
    List<DbObject> dbObjects = Lists.newArrayList();

    public void withPatchTag(Integer tagNr) {
        patchTag = dbPatchBranch + "_" + tagNr.toString();
    }
    public List<String> retrieveDbObjectsAsVcsPath() {
        return dbObjects.stream().map(DbObject::asFullPath).collect(Collectors.toList());
    }

    public void addDbObject(DbObject dbObject) {
        this.dbObjects.add(dbObject);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class DBPatchBuilder {
    }

}
