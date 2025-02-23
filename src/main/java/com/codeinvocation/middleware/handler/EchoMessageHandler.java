package com.codeinvocation.middleware.handler;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.codeinvocation.middleware.constant.MTI;
import com.codeinvocation.middleware.constant.NetworkManagementCode;
import com.codeinvocation.middleware.dto.TransactionContext;
import com.codeinvocation.middleware.validation.MessageValidation;
import com.solab.iso8583.IsoType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Qualifier("echoMessageHandler")
public class EchoMessageHandler implements IncommingMessageHandler {

	@Autowired
	@Qualifier("terminalValidation")
	private MessageValidation terminalValidation;
	
	@Override
	public void handle(TransactionContext ctx) throws Exception {
		try {
			log.info("Handling Echo Message Start");
			
			String rc = "00";
			if (!terminalValidation.valid(ctx.getReqMsg()))	{
				rc = "02";
			}				
			ctx.getRespMsg().setValue(39, rc, IsoType.ALPHA, 2);
			
		} catch (Exception e) {
			log.error("Handling Echo Message Error", e);
			ctx.getRespMsg().setValue(39, "99", IsoType.ALPHA, 2);
			
		} finally {
			log.info("Handling Echo Message Done");
		}
	}

	@Override
	public String[] keys() {
		return new String[] {
				StringUtils.join(new String[] {MTI.NETWORK_MANAGEMENT.val, "", NetworkManagementCode.ECHO.val}, ".") 
		};
	}

}
