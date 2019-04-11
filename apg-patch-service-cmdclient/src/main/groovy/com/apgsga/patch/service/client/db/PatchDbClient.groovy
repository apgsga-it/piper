package com.apgsga.patch.service.client.db
import org.springframework.util.Assert

import com.apgsga.microservice.patch.exceptions.Asserts
import com.apgsga.patch.service.client.utils.TargetSystemMappings

import groovy.json.JsonBuilder
class PatchDbClient {

	def dbConnection

	private PatchDbClient(def dbConnection) {
		super();
		this.dbConnection = dbConnection;
	}

	public def executeStateTransitionAction(def patchNumber, def toStatus) {
		def statusNum = TargetSystemMappings.instance.findStatus(toStatus) as Long;
		def id = patchNumber as Long
		def sql = 'update cm_patch_f set status = :statusNum where id = :id'
		def result = dbConnection.execute(sql,['statusNum':statusNum,'id':id])
		println result == false
		result
	}

	public def listPatchAfterClone(def status, def filePath) {
		def String sql = "SELECT id FROM cm_patch_install_sequence_f WHERE ${status}=1 AND (produktion = 0 OR chronology > trunc(SYSDATE))"
		def patchNumbers = []
		try {
			dbConnection.eachRow(sql) { row ->
				def rowId = row.ID
				patchNumbers.add(rowId)
			}

		}
		catch(Exception ex) {
			// TODO JHE(11.04.2019): because the caller will read the stdout in order to determine if all went well ... we can't write the error message. But we need to find a way to log the exception.
			println false
			return
		}
		
		// TODO (jhe, che, 19.9) have filePath passed as parameter and not preconfigured
		// Or write it stdout , but without any other println
		def listPatchFile = new File(filePath)

		if(listPatchFile.exists()) {
			listPatchFile.delete()
		}
		
		listPatchFile.write(new JsonBuilder(patchlist:patchNumbers).toPrettyString())
		println true
	}
}