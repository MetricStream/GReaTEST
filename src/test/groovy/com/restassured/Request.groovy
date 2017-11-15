package com.restassured

import java.util.List

class Request {
	String             name
	String             url
	String 			   pathParam
	String 			   queryParam
	String 			   requestBody
	String 			   method
	boolean			   benchmark
	int				   execution
	String             group
	List<Validator>    validators
}
