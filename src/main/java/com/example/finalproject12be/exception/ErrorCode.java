package com.example.finalproject12be.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

	String name();
	HttpStatus getHttpStatus();
	String getMessage();
}
