package com.cloudcomputing.cse546.controllers;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ErrorsController implements ErrorController{

	private static final String PATH = "/error";

	@RequestMapping(value = PATH)
	public String error() {
		return "Error handling";
	}

	public String getErrorPath() {
		return PATH;
	}

}
