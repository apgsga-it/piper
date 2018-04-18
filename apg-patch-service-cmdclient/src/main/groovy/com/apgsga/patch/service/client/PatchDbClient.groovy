package com.apgsga.patch.service.client
import groovy.sql.Sql
class PatchDbClient {
	
	def statusMap
	def component
	
	private PatchDbClient(def component, def statusMap) {
		super();
		this.component = component;
		this.statusMap = statusMap
	}

	public void executeStateTransitionAction(def dbProperties, def patchNumber, def toStatus) {
		println "Not implemented yet for ${dbProperties} , ${patchNumber} and ${toStatus}"
		def statusNum = statusMap[toStatus]
		if (statusNum == null) {
			println "Error , no Status mapped for ${toStatus}"
			return
		}
		def sql = "update cm_patch_f set status = ${statusNum} where id = ${patchNumber}"
		println "Executing ${sql}"
		if (component.equals("db")) {
			def dbConnection = Sql.newInstance(dbProperties.db.url, dbProperties.db.user, dbProperties.db.passwd)
			def result = dbConnection.execute(sql)
			println "Done with result: ${result}"
		} else {
			println "Done with : ${component}"
		}

	}
}
