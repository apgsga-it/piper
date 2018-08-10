import org.apache.http.util.Args

import com.apgsga.patch.service.client.PatchCli
import com.apgsga.patch.service.client.db.PatchDbCli

def client = args[0]

// We remove with "pli" or "pliDb" from args as they have no meaning for PatchCli or PatchDbCli
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

println "pliStarter couldn't find an pli to be started with name ${client}."
// Even if no error occured, we inform the caller (by returing an error status) that pli couldn't achieve any job. 
System.exit(1)