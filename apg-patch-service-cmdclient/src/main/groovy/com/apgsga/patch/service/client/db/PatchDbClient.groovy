package com.apgsga.patch.service.client.db
import org.springframework.util.Assert

import com.apgsga.microservice.patch.exceptions.Asserts
import com.apgsga.patch.service.client.utils.TargetSystemMappings

import groovy.json.JsonBuilder
class PatchDbClient {

	def config
	def dbConnection

	private PatchDbClient(def dbConnection, def config) {
		super();
		this.dbConnection = dbConnection;
		this.config = config
	}

	public def executeStateTransitionAction(def patchNumber, def toStatus) {
		def statusNum = TargetSystemMappings.instance.findStatus(toStatus) as Long;
		def id = patchNumber as Long
		def sql = 'update cm_patch_f set status = :statusNum where id = :id'
		def result = dbConnection.execute(sql,['statusNum':statusNum,'id':id])
		print result == false
		result
	}

	public def listPatchAfterClone(def status, def filePath) {
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

	// TODO (che, 25.9 ) : Better way to achieve this
	public def retrievePredecessorStatesForPatch(def patchNumber) {
		def id = patchNumber as Long
		def patchStatus = sqlRetrievePatchStatus(id)
		def relevantStatus = TargetSystemMappings.instance.relevantStateCode(patchStatus,fromToStates())
		Assert.notNull(relevantStatus, "No relevant State found for ${patchNumber} with ${patchStatus}")
		def precedessorStates = TargetSystemMappings.instance.findPredecessorStates(relevantStatus)
		print precedessorStates.join("::")
		precedessorStates
	}


	private def sqlRetrievePatchStatus(def id) {
		def sql = 'select status from  cm_patch_f where id = :patchNumber';
		def row = dbConnection.firstRow(sql, [patchNumber:id])
		Assert.notNull(row,"Patch with Id: ${id} not found")
		Assert.notNull(row.STATUS,"Unexpected Column for sql: ${sql}")
		def patchStatus =  row.STATUS
		patchStatus
	}
	
	// TODO (che , 20.9 ) Would be nice, but the required predecessor state are not complete
	private def fromToStates() {
		def sql = 'select von_status fromState, zu_status toState from cm_patch_berechtigung_f where user_id in (select user from dual)';
		def allowedStateChanges = dbConnection.rows(sql)
		Assert.isTrue(!allowedStateChanges.empty, "Unexpected result")
		allowedStateChanges
	}


}
