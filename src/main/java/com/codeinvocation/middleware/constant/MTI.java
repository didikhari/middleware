package com.codeinvocation.middleware.constant;

public enum MTI {
	NETWORK_MANAGEMENT ("0800"),
	TRANSACTIONAL("0200"),
	ADVICE("0220"),
	ADVICE_REPEAT("0221"),
	REVERSE("0400")
	;

	public String val;
	MTI(String code) {
		val = code;
	}
}
