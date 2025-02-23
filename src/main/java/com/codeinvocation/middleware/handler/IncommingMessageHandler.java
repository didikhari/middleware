package com.codeinvocation.middleware.handler;

import com.codeinvocation.middleware.dto.TransactionContext;

public interface IncommingMessageHandler {

	public void handle(TransactionContext ctx) throws Exception;
	
	public String[] keys();
}
