package com.apgsga.patch.service.client
import groovy.sql.Sql
class PatchDbClient {
	
	public void executeStateTransitionAction(def dbProperties, def patchNumber, def toStatus) {
		println "Not implemented yet for ${dbProperties} , ${patchNumber} and ${toStatus}"
		def sql = Sql.newInstance(dbProperties.db.url, dbProperties.db.user, dbProperties.db.passwd)
		def result = sql.execute("select * from cm_patch_f")
		println "result: ${result}"
		
	}
		

}
