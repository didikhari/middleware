package com.codeinvocation.middleware.constant;

public enum ProcessingCode {
	INQUIRY("380000"),
	PAYMENT("180000")
	;

	public String val;
	ProcessingCode(String code) {
		val = code;
	}
}
