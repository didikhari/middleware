package com.codeinvocation.middleware.validation;

import com.solab.iso8583.IsoMessage;

public interface MessageValidation {
	
	public boolean valid(IsoMessage reqMsg);
}
