package com.apgsga.microservice.patch.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;

/**
 * Note on usage of Lombok:
 * Ideally this would be a Value Object. But since the fields name,version and dependencyLevel are augmented after
 * initial creation and the refactoring would have been extensive. The approach with non final fields for the "mutable"
 * fields with a specific method to update -> by convention starting with "with" and
 * for all field only the @Getter Annotation seems like reasonable compromise.
 * TODO (che,jhe) : Possibly reconsider logic updating the Object.
 */
@JsonDeserialize(builder = MavenArtifact.MavenArtifactBuilder.class)
@Builder(toBuilder = true)
@EqualsAndHashCode(exclude = {"name", "version", "dependencyLevel"})
public class MavenArtifact {

    @Getter
    final String artifactId;
    @Getter
    final String groupId;
    @Getter
    String name;
    @Getter
    String version;
    @SuppressWarnings("UnusedAssignment")
    @Getter
    @lombok.Builder.Default
    Integer dependencyLevel = 0;

    public void augmentDependencyLevel() {
        dependencyLevel++;
    }

    public void withName(String name) {
        this.name = name;
    }

    public void withVersion(String version) {
        this.version = version;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class MavenArtifactBuilder {
    }


}
