package com.codeinvocation.middleware.handler;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.codeinvocation.middleware.constant.InternalRC;
import com.codeinvocation.middleware.constant.MTI;
import com.codeinvocation.middleware.constant.NetworkManagementCode;
import com.codeinvocation.middleware.dto.TransactionContext;
import com.codeinvocation.middleware.validation.MessageValidation;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Qualifier("signOffMessageHandler")
public class SignOffMessageHandler implements IncommingMessageHandler {

	@Autowired
	@Qualifier("terminalValidation")
	private MessageValidation terminalValidation;
	
	@Override
	public void handle(TransactionContext ctx) throws Exception {
		try {
			log.info("Handling Sign Off Message Start");
			
			if (!terminalValidation.valid(ctx.getReqMsg()))	{
				ctx.setResponseCode(InternalRC.INVALID_TERMINAL);
			}				
			ctx.setResponseCode(InternalRC.SUCCESS);
			
		} catch (Exception e) {
			log.error("Handling Sign Off Message Error", e);
			throw e;
			
		} finally {
			log.info("Handling Sign Off Message Done");
		}
	}
	
	@Override
	public String[] keys() {
		return new String[] {
				StringUtils.join(new String[] {MTI.NETWORK_MANAGEMENT.val, "", NetworkManagementCode.SIGN_OFF.val}, ".") 
		};
	}
}
