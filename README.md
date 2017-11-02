# GReaTEST

GReaTEST - Rest API Testing Tool

# What is GReaTEST?
GReaTEST is a  REST testing and API microbenchmarking tool. Tests are defined as basic YAML files. There is no code required. Logic is written and extensible in Groovy.

# How to Use it?
	To use GReaTEST, follow these steps:
	Validating a Rest End point. (How?)
	Capture response times of a Rest End Point on concurrent executions (How?)
	Run Gradle task and execute as JUnit (How?)
	Run the following command to execute a simple unit test:
			  gradlew run_simple_test
	Command for executing Simple unit testing: gradlew run_concurrent_test (what is the difference between this and the previous steps?)
	To run tests despite no new changes, execute the following command:
          --rerun-tasks
                                 
# What is the Test Set Syntax?
	There are five top-level test syntax elements:
	name : Provide the name of the test case that is being verified
	url: Provide the URL of a simple test, fetches given url via GET request, and checks for good response code
	test: a fully defined test (example maybe??)
	benchmark: a fully defined benchmark (example maybe??)
	validators: simple assertions about what is expected, and what is the actual value

# How to Write a Simple Test?    
	Write the test cases in a simple YAML format which contains the test data, validations, and the rest end points as follows: (What is this test case trying to achieve)

# What is Benchmarking?
Benchmarks are based off of tests: (Not sure what this means??)
Benchmarking extend the configuration elements in a test, therby allowing you to configure the REST call similarly. However, they do not perform validation on the HTTP  response, instead collect metrics. You can simulate concurrent requests, and calculate the response times of the rest end points. (How??)
  There are a few custom configuration options specific to benchmarks:
benchmark: (default is 10 if not specified) Run the benchmark to generate concurrent requests to collect data
execution: (default is 100 if unspecified)
output_file: (default is None) By default it is an XLS file, You can configure it to DB. (How??)

# Glossary Terms
Following are some of the commonly used terms:
	RestAPIPreProcessor:
		Responsible for performing all pre-processing tasks required for executing Rest testing. Some of the major tasks are:
		Reading properties form configuration file
		Establishing connection with application
		Read test cases from YAML files
	RestAPIExecutor:
		Core engine responsible for triggering tests and write back data into excel file
	RestAPIThreadExecutor:
		Executes individual tasks based on the configuration
	ResponseValidator:
		Validates response based on the provided configuration and retrieved responses
	RestAPIResultsPublisher:
		Publishes results into database (REST_TEST_RESULTS)

# Frequently Asked Questions
Q. Where to provide the application and db configuration details
	Configuration related to app and db should be provided in 'config.property' file
Q. How many types of tests can be performed?
	Two types of tests can be performed:
		Simple unit testing
		Concurrent testing
Q. Do you need to club all test cases and provide them as part of one YAML file?
	No, test cases can spread across multiple YAML files. They can be categorized based on module, scenario, and so on.
Q. Where to provide configuration details related to unit testing  and concurrent testing?
	Test cases related to unit testing should be provided as part of YAML files. Note that all files related to unit testing should be listed in 'unittestsuite.txt' file (every yaml file should be added as new line).
	Similarly for concurrent testing, yaml files should be included in 'performancetestsuite.txt' file.
Q. Where will test results be available?
	Test results will be available in the REST_TEST_RESULTS  DB table.
