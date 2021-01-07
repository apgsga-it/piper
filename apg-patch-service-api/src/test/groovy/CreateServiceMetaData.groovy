import com.apgsga.microservice.patch.api.MavenArtifact
import com.apgsga.microservice.patch.api.Package
import com.apgsga.microservice.patch.api.ServiceMetaData
import com.apgsga.microservice.patch.api.ServicesMetaData
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Lists

final def builder = MavenArtifact.builder()
def bomData = builder
        .artifactId("dm-bom")
        .groupId("com.apgsga.testapp")
        .name("apg-gradle-plugins-testsmodules/testapp/testapp-bom")
        .build()
def pkgData = Package.builder()
        .packagerName("apg-gradle-plugins-testsmodules/testapp/testapp-pkg")
        .starterCoordinates(Lists.newArrayList("com.apgsga.testapp:testapp-service"))
        .build()
def serviciceMetaData = ServiceMetaData.builder()
        .bomCoordinates(bomData)
        .baseVersionNumber("1.0.0")
        .microServiceBranch("HEAD")
        .packages(Lists.newArrayList(pkgData))
        .revisionMnemoPart("DEV-ADMIN-UIMIG")
        .serviceName("testapp-service").build()
servicesMetaData = ServicesMetaData.builder().servicesMetaData(Lists.newArrayList(serviciceMetaData)).build()
def om = new ObjectMapper()
File output = new File("../../../build/ServicesMetaData.json")
om.writeValue(output, servicesMetaData)