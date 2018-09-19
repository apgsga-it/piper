package com.apgsga.patch.service.client.db
import com.apgsga.patch.client.utils.TargetSystemMappings

import groovy.json.JsonBuilder
import groovy.sql.Sql
class PatchDbClient {

	def config
	def dbConnection

	private PatchDbClient(def dbConnection, def config) {
		super();
		this.dbConnection = dbConnection;
		this.config = config
	}

	public void executeStateTransitionAction(def patchNumber, def toStatus) {
		def statusNum = TargetSystemMappings.findStatus(config, toStatus); 
		def sql = "update cm_patch_f set status = ${statusNum} where id = ${patchNumber}".toString()
		def result = dbConnection.execute(sql)
		println result
	}

	public def listPatchAfterClone(def status, def filePath) {
		// If we don't force sql to be a String, ${status} will be replace with a "?" at the time we run the query.
		def String sql = "SELECT id FROM cm_patch_install_sequence_f WHERE ${status}=1 AND (produktion = 0 OR chronology > trunc(SYSDATE))"
		def patchNumbers = []
		dbConnection.eachRow(sql) { row ->
			def rowId = row.ID
			println "Patch ${rowId} added to the list of patch to be re-installed."
			patchNumbers.add(rowId)
		}

		// TODO (jhe, che, 19.9) have filePath passed as parameter and not preconfigured
		// Or write it stdout , but without any other println
		def listPatchFile = new File(filePath)

		if(listPatchFile.exists()) {
			listPatchFile.delete()
		}

		listPatchFile.write(new JsonBuilder(patchlist:patchNumbers).toPrettyString())
	}

	public def retrieveCurrentPatchState(def patchNumber) {
		def sql = "select status from  cm_patch_f where id = ${patchNumber}".toString()
		def result = dbConnection.firstRow(sql)
		print result
		result
	}


}
