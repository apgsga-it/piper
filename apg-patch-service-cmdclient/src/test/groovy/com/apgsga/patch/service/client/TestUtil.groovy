package com.apgsga.patch.service.client

class TestUtil {
	
	static def getLastProdRevisionLine(String lines) {
		// Looking for the line which is for us interesting -> should contain "lastProdRevision"
		def searchedLine = null
		lines.eachLine{ line ->
			if (line != null) {
				if(line.contains("lastProdRevision")) {
					searchedLine = line
				}
			}
		}
		return searchedLine
	}
	
	static def getRevisionLine(String lines) {
		// Looking for the line which is for us interesting -> should contain "fromRetrieveRevision"
		def searchedLine = null
		lines.eachLine{ line ->
			if (line != null) {
				if(line.contains("fromRetrieveRevision")) {
					searchedLine = line
				}
			}
		}
		return searchedLine
	}

}
