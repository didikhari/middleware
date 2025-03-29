package com.codeinvocation.middleware.constant;

public enum MTI {
	NETWORK_MANAGEMENT (0x800),
	TRANSACTIONAL(0x200),
	ADVICE(0x220),
	ADVICE_REPEAT(0x221),
	REVERSE(0x400)
	;

	public int val;
	MTI(int code) {
		val = code;
	}
	
	public String getString() {
		return String.format("%04x", val);
	}
}
