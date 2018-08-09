import org.apache.http.util.Args

import com.apgsga.patch.service.client.PatchCli
import com.apgsga.patch.service.client.db.PatchDbCli

def client = args[0]
args = args - "pli"
args = args - "pliDb"
 

if(client.equalsIgnoreCase("pli")) {
	println "Starting pli client"
	System.exit(PatchCli.create().process(args).returnCode)
}

if(client.equalsIgnoreCase("pliDb")) {
	println "Starting pliDb client"
	System.exit(PatchDbCli.create().process(args).returnCode)
}
