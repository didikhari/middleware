package com.codeinvocation.middleware.handler;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.codeinvocation.middleware.constant.MTI;
import com.codeinvocation.middleware.constant.ProcessingCode;
import com.codeinvocation.middleware.dto.TransactionContext;

@Service
@Qualifier("paymentMessageHandler")
public class PaymentMessageHandler implements IncommingMessageHandler {

	@Override
	public void handle(TransactionContext ctx) throws Exception {
		
	}

	@Override
	public String[] keys() {
		return new String[] {
				StringUtils.join(new String[] {MTI.TRANSACTIONAL.getString(), ProcessingCode.PAYMENT.val}, ".") 
		};
	}
}
