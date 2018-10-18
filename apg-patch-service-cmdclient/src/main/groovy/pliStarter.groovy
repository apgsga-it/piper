import org.apache.http.util.Args

import com.apgsga.patch.service.client.PatchCli
import com.apgsga.patch.service.client.db.PatchDbCli
import com.apgsga.patch.service.client.revision.PatchRevisionCli

def client = args[0]

// We remove with "pli", "pliDb" or "pliRev from args as they have no meaning for PatchCli or PatchDbCli
args = args - "pli"
args = args - "pliDb"
args = args - "pliRev"

 

if(client.equalsIgnoreCase("pli")) {
	System.exit(PatchCli.create().process(args).returnCode)
}

if(client.equalsIgnoreCase("pliDb")) {
	System.exit(PatchDbCli.create().process(args).returnCode)
}

if(client.equalsIgnoreCase("pliRev")) {

	System.exit(PatchRevisionCli.create().process(args).returnCode)
}

println "pliStarter couldn't find an pli to be started with name ${client}."
System.exit(1)