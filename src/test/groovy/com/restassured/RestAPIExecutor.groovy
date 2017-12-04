package com.restassured;

import static org.junit.Assert.*

import static io.restassured.RestAssured.*
import static io.restassured.matcher.RestAssuredMatchers.*
import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Row
import org.hamcrest.Matchers.*
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

class RestAPIExecutor {

	static Properties property = new Properties();

	static def unitTests = []

	static def performanceTests = []

	def map = []

	static HSSFWorkbook excel = new HSSFWorkbook();

	Row row = null;

	/**
	 * this method authenticate the instance, setting the session and csrf token
	 * loads facade yaml files to the list
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		unitTests = new RestAPIPreProcessor().preProcessUnitTestSuite(property);
		performanceTests = new RestAPIPreProcessor().preProcessPerformanceTestSuite(property);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		/*FileOutputStream outputStream = null
		 try {
		 outputStream = new FileOutputStream("TestResults.xls")
		 excel.write(outputStream)
		 } catch(Exception e) {
		 } finally {
		 outputStream.close();
		 }*/
	}

	@Test
	public void unitTest() {
		ExecutorService executor = null
		RestAPIThreadExecutor restAPIThreadExecutor = null
		def futureResponses = []
		Future<String> future = null
		def threadCounter = 0
		executor = Executors.newFixedThreadPool(10)
		unitTests.each {
			map = it
			threadCounter++
			restAPIThreadExecutor = new RestAPIThreadExecutor(property, map)
			future = executor.submit(restAPIThreadExecutor)
			futureResponses.add(future)
		}
		for (item in futureResponses) {
			while (!item.isDone()) {
				Thread.sleep(500)
			}
			println "===> test completed"
		}
		executor.shutdown()
		try {
			executor.awaitTermination(3600, TimeUnit.SECONDS)
		} catch (InterruptedException e) {
			println "error occurred while executing unit testing"
		}
	}

	@Test
	public void performanceTest() {
		ExecutorService executor = null
		RestAPIThreadExecutor restAPIThreadExecutor = null
		def futureResponses = []
		def testCaseName
		Future<String> future = null
		performanceTests.each {
			map = it
			testCaseName = it.get("testCaseName")
			def concurrentEnabled = (map.get("request")).getAt("benchmark")
			def noOfExecutions = (map.get("request")).getAt("execution")
			executor = Executors.newFixedThreadPool(noOfExecutions)
			noOfExecutions.times {
				restAPIThreadExecutor = new RestAPIThreadExecutor(property, map)
				future = executor.submit(restAPIThreadExecutor)
				futureResponses.add(future)
			}
			def printStatement = '===>testConcurrent completed'
			for (item in futureResponses) {
				while (!item.isDone()) {
					Thread.sleep(2000)
				}
				RestAPIResultsPublisher.insertData(RestAPIPreProcessor.sqlObject, item.get(), testCaseName)
			}
		}
		executor.shutdown()
		try {
			executor.awaitTermination(3600, TimeUnit.SECONDS)
		} catch (InterruptedException e) {
			println "error occurred while executing concurrent testing"
		}
	}
}

