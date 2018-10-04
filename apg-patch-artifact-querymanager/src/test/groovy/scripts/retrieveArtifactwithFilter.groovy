package scripts
import java.io.File
import java.io.FileWriter

import com.apgsga.artifact.query.ArtifactManager

def artificts = ArtifactManager.create("D:/apg/maven/repository").getArtifactsWithNameFromBom("9.1.0.ADMIN-UIMIG-SNAPSHOT")
println artificts
