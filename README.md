# GReaTEST
==========

# Table of Contents

- [What is GReaTEST?](#what-is-greatest)
- [How to Use it?](#how-to-use-it)
- [What is the Test Set Syntax?](#what-is-the-test-set-syntax)
- [How to Write a Simple Test?](#how-to-write-a-simple-test)
- [What is Benchmarking?](#what-is-benchmarking)
- [Glossary Terms](#glossary-terms)
- [Frequently Asked Questions](#frequently-asked-questions)
- [Feedback and Contributions](#feedback-and-contributions)



# What is GReaTEST?
- GReaTEST - Rest API Testing Tool
- GReaTEST is a  REST testing and API microbenchmarking tool. Tests are defined as basic YAML files. There is no code required. Logic is written and extensible in Groovy.

# How to Use it?
## To use GReaTEST, follow below steps:
	- Run Gradle task or Execute as JUnit test case
	- Command to execute a simple unit test which executes the test cases for one iteration and captures unit test results
		gradlew run_simple_test
	- Command to execute concurrent tests which executes the test cases for specified number of iterations and captures the time taken for executing individual test case. Concurrent tests will not validate assertions
		gradlew run_concurrent_test
	- To run tests despite of no changes, execute the command --rerun-tasks
                                 
# What is the Test Set Syntax?
## There are five top-level test syntax elements:
	- name : Provide the name of the test case that is being verified
	- url: Provide the URL of a simple test, fetches given url via GET request, and checks for good response code
	- test: a fully defined test
	- benchmark: a fully defined benchmark
	- validators: simple assertions about what is expected, and what is the actual value

# How to Write a Simple Test?    
	- Write the test cases in a simple YAML format which contains the test data, validations, and the rest end points as follows: 
	
	config:
  	testset: "Sample Test"
  	QueryPropertyFile: "sample_test_quries.properties"
	  ParametersFile: "sample_test_parameters.properties"
	tests:
	
	  - name: "Test to compare the count of total alerts defined in system."
	    url: "/alerts"
	    pathParam: null
	    queryParam: null
	    requestBody: null
	    method: "GET"
	    group: "Successful"
	    validators: # operator is applied as: <actual> <operator> <expected>
	     - compare: {assertKey: "statusCode", comparator: "eq", expected: 200}
	     - compare: {assertKey: "type", comparator: "eq", expected: "alertResponse"}
	     - compare: {assertKey: "total", comparator: "eq", expected:  "ExpectedResultQueryName:query1"}
	  - name: "Test query param  alert search using any text against the columns:- Alert Name"
	    url: "/alerts"
	    pathParam: 
	    queryParam: "alert=TestAlert"
	    requestBody: null
	    method: "GET"
	    group: "Successful"
	    validators: # operator is applied as: <actual> <operator> <expected>
	     - compare: {assertKey: "statusCode", comparator: "eq", expected: 200}
	     - compare: {assertKey: "type", comparator: "eq", expected: "alertResponse"}
	     - compare: {assertKey: "total", comparator: "eq", expected:  "ExpectedResultQueryName:query2"}

# What is Benchmarking?
	- Benchmarking extend the configuration elements in a test, therby allowing you to configure the REST call similarly. However, they do not perform validation on the HTTP response, instead collect metrics. You can simulate concurrent requests, and calculate the response times of the rest end points.
	- There are a few custom configuration options specific to benchmarks:
		- benchmark: (default is 10 if not specified) Run the benchmark to generate concurrent requests to collect data
		- execution: (default is 100 if unspecified)
        - output_file: By default results will be pushed to DB, You can configure it to XLS by providing appropriate value to the property in config file.

# Glossary Terms
## Following are some of the commonly used terms:
	## RestAPIPreProcessor:
		- Responsible for performing all pre-processing tasks required for executing Rest testing. Some of the major tasks are:
		- Reading properties form configuration file
		- Establishing connection with application
		- Read test cases from YAML files
	## RestAPIExecutor:
		- Core engine responsible for triggering tests and write back data into excel file
	## RestAPIThreadExecutor:
		- Executes individual tasks based on the configuration
	## ResponseValidator:
		- Validates response based on the provided configuration and retrieved responses
	## RestAPIResultsPublisher:
		- Publishes results into database (REST_TEST_RESULTS)

# Frequently Asked Questions
	## Where to provide the application and db configuration details
		- Configuration related to app and db should be provided in 'config.property' file
	## How many types of tests can be performed?
		- Two types of tests can be performed:
			- Simple unit testing
			- Concurrent testing
	## Do you need to club all test cases and provide them as part of one YAML file?
		- No, test cases can spread across multiple YAML files. They can be categorized based on module, scenario, and so on.
	## Where to provide configuration details related to unit testing  and concurrent testing?
		- Test cases related to unit testing should be provided as part of YAML files. Note that all files related to unit testing should be listed in 'unittestsuite.txt' file (every yaml file should be added as new line).
		Similarly for concurrent testing, yaml files should be included in 'performancetestsuite.txt' file.
	## Where will test results be available?
		- Test results will be available in the REST_TEST_RESULTS DB table.
