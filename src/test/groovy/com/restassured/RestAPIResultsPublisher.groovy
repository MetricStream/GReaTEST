package com.restassured

import groovy.sql.Sql

class RestAPIResultsPublisher {
	
	def static retrieveConnection (Properties property) {
		return Sql.newInstance("jdbc:oracle:thin:@"+property.getProperty("dbUrl")+":"+property.getProperty("oraclePort")+":"+property.getProperty("sid"),property.getProperty("dBUserName"),property.getProperty("dBPassword"),
			"oracle.jdbc.pool.OracleDataSource")
	}
	def static insertData (def Sql sqlObject, def Map resultMap, def testCaseName){
		sqlObject.execute("INSERT INTO REST_TEST_RESULTS (test_id, test_case_name , type , executed_on, assert_key, comparator, expected, actual, result, time_taken_msec) values ("+
			RestAPIPreProcessor.executionSequence+", '"+testCaseName+"','${resultMap.type}', sysdate, '${resultMap.assertkey}', '${resultMap.comparator}', '${resultMap.expected}', '${resultMap.actual}', '${resultMap.resultcode}', '${resultMap.timetaken}')")
		sqlObject.commit()
	}
	def static closeConnection (def Sql sqlObject) {
		sqlObject.close()
	}
}
