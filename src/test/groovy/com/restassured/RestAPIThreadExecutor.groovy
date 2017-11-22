package com.restassured;

import static io.restassured.RestAssured.*
import static io.restassured.matcher.RestAssuredMatchers.*
import static io.restassured.RestAssured.*
import static io.restassured.matcher.RestAssuredMatchers.*
import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

import java.util.concurrent.Callable

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.hamcrest.Matchers.*

import groovy.json.JsonSlurper
import io.restassured.builder.RequestSpecBuilder
import io.restassured.response.Response
import io.restassured.specification.RequestSpecification

public class RestAPIThreadExecutor implements Callable {

	static def sql
	static def properties = new Properties();
	public def myList
	def queryProperties = new Properties();

	def paramProperties = new Properties();

	def queryInputStream;

	def parameterInputStream;

	public static RequestSpecBuilder builder = new RequestSpecBuilder()
	
	public RestAPIThreadExecutor(Properties properties, Map map) {
		this.properties = properties
		this.myList = map
	}

	@Override
	public Map<String, String> call() {
		return testCases(myList)
	}

	/**
	 * this method takes each yaml files and performing the testing operation
	 */

	public Map<String, String> testCases(Map list) {
		def resultsMap = [:]
		long startTime = System.currentTimeMillis();
		Request test = list.get("request")
		loadProperties(list.get("queryFile"),list.get("parameterFile"))
		def getUrl = properties.getProperty("BaseURI") + properties.getProperty("BasePath") + test.url
		Response response = null;

		if(test.pathParam != null ) {
			getUrl = getUrl + "/" + test.pathParam
		}
		if(test.queryParam != null ) {
			getUrl = getUrl + "?" + test.queryParam
		}
		// Execute the REST API, and get the response
		switch(test.method) {
			case "GET":
				response = testGet(getUrl)
				break
			case "POST":
				response = testPost(getUrl,test.requestBody,paramProperties)
				break
			case "PUT":
				response = testPut(getUrl,test.requestBody,paramProperties)
				break
		}
		println "Test Case Name : " +test.name+", executions: "+test.execution
		// Loop through all the Validators and extract the different values in each comparison
		if(test.execution > 0) {
			resultsMap.putAt('type', 'concurrent test')
		} else {
			resultsMap.putAt('type', 'unit test')
			test.getValidators().each { val ->
				new ResponseValidator().assertResponse(response,val,sql,queryProperties,list.get("testSetName"),test.name, (System.currentTimeMillis() - startTime), resultsMap)
			}
		}
		resultsMap.putAt('seq', RestAPIPreProcessor.executionSequence)
		resultsMap.putAt('testcasename',test.name)
		resultsMap.putAt('type', 'concurrent test')
		resultsMap.putAt('timetaken', (System.currentTimeMillis() - startTime))
		return resultsMap
	}

	/**
	 * rest call using get method
	 * @param url
	 * @return
	 */
	Response testGet(String url) {
		RequestSpecification requestSpec = builder.build()
		return given().spec(requestSpec).when().get(url)
	}

	/**
	 * rest call using post method
	 * @param url
	 * @param requestBody
	 * @param paramProperties
	 * @return
	 */
	Response testPost(String url,String  requestBody, Properties paramProperties) {
		def jsonSlurper = new JsonSlurper()
		def body,paramName
		if(requestBody != null && requestBody.contains("ParamName")) {
			paramName = requestBody.replace("ParamName:", "")
			body = paramProperties.getProperty(paramName)
		}else (requestBody != null){ body = requestBody }
		def object = jsonSlurper.parseText(body)
		RequestSpecification requestSpec = builder.build()
		return given().spec(requestSpec).body(object).when().post(url)

	}

	/**
	 * rest call using post method
	 */
	Response testPut(String url,String  requestBody, Properties paramProperties) {
		def jsonSlurper = new JsonSlurper()
		def body,paramName
		if(requestBody != null && requestBody.contains("ParamName")) {
			paramName = requestBody.replace("ParamName:", "")
			body = paramProperties.getProperty(paramName)
		}else (requestBody != null){ body = requestBody }
		def object = jsonSlurper.parseText(body)
		RequestSpecification requestSpec = builder.build()
		return given().spec(requestSpec).body(object).when().put(url)
	}

	/**
	 * loading the parameter and query property to Properties
	 * @param queryFile
	 * @param parameterFile
	 */
	void loadProperties(String queryFile, String parameterFile) {
		Path path = Pats.get("src/queryproperties/"+queryFile)
		queryInputStream = new FileInputStream(path.toString())
		queryProperties.load(queryInputStream)
		if(queryInputStream != null) {
			queryInputStream.close()
		}
		path = Pats.get("src/params/"+parameterFile)
		parameterInputStream = new FileInputStream(path.toString())
		paramProperties.load(parameterInputStream)
		if(paramInputStream != null) {
			paramInputStream.close()
		}
	}
}