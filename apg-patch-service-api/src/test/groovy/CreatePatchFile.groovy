import com.apgsga.microservice.patch.api.MavenArtifact
import com.apgsga.microservice.patch.api.Patch
import com.apgsga.microservice.patch.api.Service
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Lists

def patch = Patch.builder().patchNumber("1")
        .services(Lists.newArrayList(
                Service.builder().serviceName("testapp-service").
                        artifactsToPatch(Lists.newArrayList(
                                MavenArtifact.builder()
                                        .artifactId("testapp-module")
                                        .groupId("com.apgsga.testapp")
                                        .version("1.0.0.DEV-ADMIN-UIMIG-SNAPSHOT").build(),
                        )).build()
        )).build()
def om = new ObjectMapper()
File output = new File("../../../build/Patch001.json")
om.writeValue(output, patch)