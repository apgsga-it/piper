package com.apgsga.patch.service.client
import groovy.sql.Sql
class PatchDbClient {
	
	def statusMap = [EntwicklungInstallationsbereit:2,InformatiktestInstallationsbereit:15, ProduktionInstallationsbereit:65, Entwicklung:0, Informatiktest:20, Produktion:80]
	
	public void executeStateTransitionAction(def dbProperties, def patchNumber, def toStatus) {
		println "Not implemented yet for ${dbProperties} , ${patchNumber} and ${toStatus}"
		def statusNum = statusMap[toStatus]
		if (statusNum == null) {
			println "Error , no Status mapped for ${toStatus}"
			return
		}
		def dbConnection = Sql.newInstance(dbProperties.db.url, dbProperties.db.user, dbProperties.db.passwd)
		def sql = "update cm_patch_f set status = ${statusNum} where id = ${patchNumber}"
		println "Executing ${sql}"
		def result = dbConnection.execute(sql)
		println "Done with result: ${result}"
	}
}
