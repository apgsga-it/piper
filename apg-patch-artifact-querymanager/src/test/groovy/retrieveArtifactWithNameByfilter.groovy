import java.io.File
import java.io.FileWriter

import com.apgsga.artifact.query.ArtifactManager

def artificts = ArtifactManager.create("D:/apg/maven/repository").getArtifactsWithNameAsMap("9.0.6.ADMIN-UIMIG-SNAPSHOT")
println artificts
