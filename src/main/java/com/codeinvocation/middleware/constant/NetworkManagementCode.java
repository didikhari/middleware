package com.codeinvocation.middleware.constant;

public enum NetworkManagementCode {
	ECHO("301"),
	SIGN_ON("001"),
	SIGN_OFF("002")
	;

	public String val;
	NetworkManagementCode(String code) {
		val = code;
	}
	
}
