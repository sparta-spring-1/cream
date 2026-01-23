package com.sparta.cream.exception;

import org.springframework.http.HttpStatus;

public interface BaseCode {
	HttpStatus getStatus();

	String getMessage();
}
