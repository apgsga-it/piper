package scripts
import java.io.File
import java.io.FileWriter

import com.apgsga.artifact.query.ArtifactManager


def properties = ArtifactManager.create("D:/apg/maven/repository").getVersionsProperties("9.1.0.ADMIN-UIMIG-SNAPSHOT")
println properties
def propertyFile = new File('build/versions.properties');
properties.store(new FileWriter(propertyFile),
		"Generated versions property file from: dm-bom, com.affichage.common.maven, 9.1.0.ADMIN-UIMIG-SNAPSHOT");