package com.codeinvocation.middleware.validation;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.solab.iso8583.IsoMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Qualifier("terminalValidation")
public class TerminalValidation implements MessageValidation {

	@Override
	public boolean valid(IsoMessage reqMsg) {		
		try {
			log.info("Terminal Validation Start");
			// TODO Perform Terminal ID, Status Validation
			return true;
			
		} catch (Exception e) {
			log.error("Terminal Validation Error", e);
			
		} finally {
			log.info("Terminal Validation Done");
		}
		return false;
	}

}
