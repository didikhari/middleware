package com.codeinvocation.middleware.constant;

public enum InternalRC {

	SUCCESS("00"),
	TIMEOUT("03"),
	INVALID_MESSAGE("01"),
	INVALID_TERMINAL("02"),
	GENERAL_ERROR("99")
	;

	public String val;
	InternalRC(String code) {
		val = code;
	}
}
