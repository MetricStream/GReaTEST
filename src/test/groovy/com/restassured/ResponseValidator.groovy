package com.restassured

import static org.junit.Assert.*

import io.restassured.response.Response

class ResponseValidator {
	
	static final String SUCCESS_MESSAGE = "SUCCESS";
	
	static final String FAILURE_MESSAGE = "FAILURE";
	
	BufferedWriter bufferedWriter = null;
	
	public void assertResponse(Response response,Validator val,def sql,Properties queryProperties,String testSetName, String testCaseName, def totalTime, def resultsMap) {
	  println val.compare
	  FileWriter fileWriter = new FileWriter("src/outputfiles/"+testSetName+".txt",true);
	  BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)
	  def responseExtract, comparator, expected
	  
	  bufferedWriter.write("TestCase Name : "+testCaseName)
	  bufferedWriter.newLine()
	  def testName = testCaseName
	  
	  //splitting the validators 
	  val.compare.each { k,v ->
		  switch(k) {
			 case "assertKey":
			  	if(v.equalsIgnoreCase("statusCode")) {
					  responseExtract = response.statusCode
				}else if(v.equalsIgnoreCase("responseBody")) {
					responseExtract = response
				}else{
					responseExtract = response.path(v)
				}		
				break
			case "comparator":
				comparator = v
				break
			case "expected":
		  		expected = deriveExpectedValue(v,queryProperties,sql)
				break
		}
	  }
  
	  // Now that we have the comparison to be done in these 3 variables,
	  // execute the actual comparison
	  println responseExtract + " " + comparator + " " + expected
	  
	  bufferedWriter.write(val.compare.toString() +" Actual : "+responseExtract)
	  resultsMap.putAt('assertkey',val.compare.getAt('assertKey'))
	  resultsMap.putAt('comparator',comparator)
	  resultsMap.putAt('expected',expected)
	  resultsMap.putAt('actual',responseExtract)
	  bufferedWriter.newLine()
	  
	  boolean assertResult;
	  switch (comparator) {
		case "eq":
		  	assertResult = equalsAssert(responseExtract,expected)
			break
		case "ge":
			assertResult = assertGreaterThanEquals(responseExtract,expected)
			break
		case "gt":
			assertResult = assertGreaterThan(responseExtract,expected)
			break
		case "le":
			assertResult = assertLessThanEquals(responseExtract, expected)
			break
		case "lt":
			assertResult = assertLessThan(responseExtract, expected)
			break
		case "count_eq":
			assertResult = assertCountEquals(responseExtract, expected)
			break
		case "str_eq":
			assertResult = assertStringEquals(responseExtract, expected)
			break
		case "ne":
			assertResult = assertNotEquals(responseExtract, expected)
			break
		case "contains":
			assertResult = assertContains(responseExtract, expected)
			break
	    case "contained_by":
			assertResult = assertContainedBy(responseExtract, expected)
			break
		case "type":	
			assertResult = assertType(responseExtract, expected)
			break;
		case "regex":
			break
		default:
		  break
		
	  }
	  if(assertResult){
		  println SUCCESS_MESSAGE
		  bufferedWriter.write("Result :" + SUCCESS_MESSAGE)
		  resultsMap.putAt('resultcode',SUCCESS_MESSAGE)
		  bufferedWriter.newLine()
	  }else {
		  println FAILURE_MESSAGE
		  bufferedWriter.write("Result :" + FAILURE_MESSAGE)
		  resultsMap.putAt('resultcode', FAILURE_MESSAGE)
		  bufferedWriter.newLine()
	  }
	  resultsMap.putAt('timetaken', totalTime)
	  resultsMap.putAt('testcasename',testCaseName)
	  RestAPIResultsPublisher.insertData(RestAPIPreProcessor.sqlObject, resultsMap, testName)
	  bufferedWriter.write("Time taken :" + totalTime + " msec")
	  bufferedWriter.newLine()
	  bufferedWriter.write("-------------------------------------------------------------------------------------")
	  bufferedWriter.newLine()
	  bufferedWriter.close();
	}
	
	/**
	 * deriving the expected value, if it is a query then fetching from the database and returning the result
	 * @param v
	 * @param queryProperties
	 * @param sql
	 * @return
	 */
	
	def deriveExpectedValue(String v, Properties queryProperties, def sql) {
		def expected
		if(v.contains("ExpectedResultQueryName")) {
			def queryName = v.replace("ExpectedResultQueryName:", "")
			def query = queryProperties.getProperty(queryName)
			//println query
			sql.query(query) { resultSet ->
				while (resultSet.next()) {
					expected = resultSet.getString(1)
				  	//println "Value :" +expected
				}
			}
		}else if(v.contains("ExpectedResultQuery")) {
			def query = v.replace("ExpectedResultQuery:", "")
			//println query
			sql.query(query) { resultSet ->
				while (resultSet.next()) {
					expected = resultSet.getString(1)
					//println "Value :" +expected
				}
			}
		}else  {
			expected = v
		}
		return expected
	} 
	
	/**
	 * asserting equals 
	 * @param responseExtract
	 * @param expected
	 * @return
	 */
	boolean equalsAssert(def responseExtract, def expected) {
		if(responseExtract == null) {
			//assert expected == null
			return expected == null
		}else if(responseExtract instanceof String || responseExtract instanceof GString) {
			assert responseExtract.equals(expected)
			return responseExtract.equals(expected)
		}else {
			//assert (responseExtract as Integer) == (expected as Integer)
			return (responseExtract as Integer) == (expected as Integer)
		}	
	}
	
	/**
	 * asserting greater than equals to comparator
	 * @param responseExtract
	 * @param expected
	 * @return
	 */
	boolean assertGreaterThanEquals(def responseExtract,def expected) {
		
		//assert (responseExtract as Integer) >= (expected as Integer)
		return (responseExtract as Integer) >= (expected as Integer)
	}

	/**
	 * asserting greater than comparator
	 * @param responseExtract
	 * @param expected
	 * @return
	 */
	boolean assertGreaterThan(def responseExtract,def expected) {
		//assert (responseExtract as Integer) > (expected as Integer)
		return (responseExtract as Integer) > (expected as Integer)
	}
	
	/**
	 * asserting less than equals to comparator
	 * @param responseExtract
	 * @param expected
	 * @return
	 */
	boolean assertLessThanEquals(def responseExtract,def expected) {
		//assert (responseExtract as Integer)<= (expected as Integer)
		return (responseExtract as Integer)<= (expected as Integer)
	}
	
	/**
	 * asserting greater than comparator
	 * @param responseExtract
	 * @param expected
	 * @return
	 */
	boolean assertLessThan(def responseExtract,def expected) {
		//assert (responseExtract as Integer) < (expected as Integer)
		return (responseExtract as Integer) < (expected as Integer)
	}
	
	
	/**
	 * asserting count equals comparator
	 * @param responseExtract
	 * @param expected
	 * @return
	 */
	boolean assertCountEquals(def responseExtract,def expected) {
		//assert responseExtract.toString().size() as int == (expected as int)
		return responseExtract[0].toString().size() as Integer == (expected as Integer)
	}
	
	
	/**
	 * asserting string equals comparator
	 * @param responseExtract
	 * @param expected
	 * @return
	 */
	boolean assertStringEquals(def responseExtract,def expected) {
		//assert responseExtract.equals(expected)
		return responseExtract.equals(expected)	
	}
	
	/**
	 * asserting not equals to comparator
	 * @param responseExtract
	 * @param expected
	 * @return
	 */
	boolean assertNotEquals(def responseExtract,def expected) {
		//assert !responseExtract.equals(expected)
		return !responseExtract.equals(expected)
	}
	
	/**
	 * asserting contains comparator
	 * @param responseExtract
	 * @param expected
	 * @return
	 */
	boolean assertContains(def responseExtract,def expected) {
		//assert responseExtract.toString().contains(expected)
		return responseExtract.toString().contains(expected)
	}
	
	/**
	 * asserting containedBy comparator
	 * @param responseExtract
	 * @param expected
	 * @return
	 */
	boolean assertContainedBy(def responseExtract,def expected) {
		//assert expected.toString().contains(responseExtract)
		return expected.toString().contains(responseExtract)
	}
	
	/**
	 * asserting data type comparator
	 * @param responseExtract
	 * @param expected
	 * @return
	 */
	boolean assertType(def responseExtract,def expected) {
		if(expected.toString().equalsIgnoreCase("Integer"))	{
		//	assert responseExtract instanceof Integer
			return responseExtract instanceof Integer
		}else if(expected.toString().equalsIgnoreCase("Date")){
			//assert responseExtract instanceof Date
			return responseExtract instanceof Date
		}else if(expected.toString().equalsIgnoreCase("String")){
			//assert responseExtract instanceof String
			return responseExtract instanceof String 
		}else {
			return false;
		}
	}
}
