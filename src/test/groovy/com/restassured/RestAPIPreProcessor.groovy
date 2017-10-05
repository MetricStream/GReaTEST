package com.restassured

import static io.restassured.RestAssured.*

import org.yaml.snakeyaml.TypeDescription
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

import groovy.sql.Sql
import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.Header

class RestAPIPreProcessor {

	def builder = new RequestSpecBuilder()

	def static sqlObject

	def static executionSequence

	def preProcessUnitTestSuite(Properties property) {

		def requests = []
		requests = readUnitTestSuite(property, requests, false)
		requests
	}

	def preProcessPerformanceTestSuite(Properties property) {

		def requests = []
		requests = readUnitTestSuite(property, requests, true)
		//deleting already existing output files.
		deleteExistingOutputFiles();
		retrieveCSRFandSession(property)
		requests
	}

	private readUnitTestSuite(Properties property, List requests, boolean perfTestSuite) {
		def inStream = new FileInputStream("src/config.properties")
		property.load(inStream)

		def file;
		if(perfTestSuite) {
			file = new File("src/testcases/performancetestsuite.txt")
		} else {
			file = new File("src/testcases/unittestsuite.txt")
		}

		def facadeList =  file.readLines()
		def cstr = new Constructor ( TestSet.class )
		def pDesc = new TypeDescription( TestSet.class )
		pDesc.putListPropertyType("tests", Request.class )
		cstr.addTypeDescription( pDesc )
		def qDesc = new TypeDescription( Request.class )
		qDesc.putListPropertyType( "validators", Validator.class )
		cstr.addTypeDescription( qDesc )
		for(String facade:facadeList) {
			def yaml = new Yaml( cstr )
			// Read the YAML file
			def testSet = (TestSet) yaml.load(("src/testcases/"+facade as File).text)
			testSet.getTests().each { test ->
				if(property.getProperty("TestSuite").equalsIgnoreCase(test.group)){
					def list = [:]
					list['testSetName'] = testSet.getConfig().testset
					list['testCaseName'] = test.name
					list['request'] = test
					list['queryFile'] = testSet.getConfig().QueryPropertyFile
					list['parameterFile'] = testSet.getConfig().ParametersFile
					requests.add(list)
				}
			}
		}
		requests
	}

	def retrieveCSRFandSession(Properties property) {
		RestAssured.useRelaxedHTTPSValidation()
		def header = new Header("Content-Type", "application/x-www-form-urlencoded")

		// Login to the app and get the cookies
		def response1 = given().formParam("username", property.getProperty("UserName")).formParam("password",property.getProperty("Password")).header(header).request().post(property.getProperty("BaseURI")+"/metricstream/auth/basic")
		println "CSRF = " + response1.getCookie("Csrf-Token")
		println "JESSION = " + response1.getCookie("JSESSION_ID")

		// Initialize a RequestSpec and add the cookies obtained above
		builder.addHeader("X-Csrf-Token", response1.getCookie("Csrf-Token"))
		builder.addCookies([ "JSESSION_ID" : response1.getCookie("JSESSION_ID") ])
		builder.setContentType("application/json; charset=UTF-8")
		builder.addHeader("accept", "application/json")
		if (response1 == null){
			println "Authentication Failed"
			System.exit();
		} else if(response1.statusCode() != 302){
			println "Authentication Failed" + " | Status Code : "+response1.statusCode()
			System.exit();
		}

		// connecting to the db for assertion
		sqlObject = RestAPIResultsPublisher.retrieveConnection(property)
		retriveExecutionSquence(sqlObject)
	}

	def retriveExecutionSquence(sqlObject) {
		sqlObject.eachRow('select rest_test_seq.nextval as s from dual') { row ->
			executionSequence = row.s
		}
	}

	/**
	 * deleting already existing output files.
	 */
	def deleteExistingOutputFiles() {
		def outputFolder = new File("src/outputfiles");
		def outputFiles = outputFolder.list()
		for(String outputFile: outputFiles){
			def currentFile = new File(outputFolder.getPath(),outputFile);
			currentFile.delete();
		}
	}
}
