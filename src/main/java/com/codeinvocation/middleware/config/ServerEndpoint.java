package com.codeinvocation.middleware.config;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;

import com.codeinvocation.middleware.constant.InternalRC;
import com.codeinvocation.middleware.dto.TransactionContext;
import com.codeinvocation.middleware.handler.DynamicMessageHandler;
import com.codeinvocation.middleware.handler.IncommingMessageHandler;
import com.solab.iso8583.CustomIsoMessage;
import com.solab.iso8583.MessageFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@MessageEndpoint
public class ServerEndpoint {

	@Autowired
	private MessageFactory<CustomIsoMessage> messageFactory;
	
	@Autowired
	private DynamicMessageHandler dynamicMessageHandler;

	@Transformer(inputChannel="fromTcp", outputChannel="toHandler")
    public TransactionContext convert(Message<byte[]> messages) {
		String clientIpAddress = (String) messages.getHeaders().get("ip_address");
		Integer clientPort = (Integer) messages.getHeaders().get("ip_tcp_remotePort");
		String connectionId = ((UUID) messages.getHeaders().get("id")).toString();
		Long timestamp = (Long) messages.getHeaders().get("timestamp");
		byte[] payLoad = messages.getPayload();
		log.debug("fromTcp-toHandler [{}]", connectionId);
		
		TransactionContext ctx = TransactionContext.builder()
				.clientIpAddress(clientIpAddress)
				.clientPort(clientPort)
				.rawReqMsg(payLoad)
				.connectionId(connectionId)
				.requestTimestamp(timestamp)
				.build();
		
		if (payLoad.length != 0) {
			try {
				CustomIsoMessage reqMsg = messageFactory.parseMessage(payLoad, 0);
				ctx.setReqMsg(reqMsg);
				log.info("Received Request [{}]", reqMsg.dumpField(true, 
		    			ctx.getConnectionId(), ctx.getClientIpAddress(), 
		    			ctx.getClientPort(), ctx.getRequestTimestamp()));
				return ctx;
				
	    	} catch (Exception e) {
				log.error("Error when parsing message", e);
			} 
		}    	
        return null;
    }

    @ServiceActivator(inputChannel="toHandler")
    public byte[] handle(@Payload TransactionContext ctx) throws Exception {
    	ctx.setRespMsg(messageFactory.createResponse(ctx.getReqMsg()));
    	
    	IncommingMessageHandler incommingMessageHandler = dynamicMessageHandler.getMessageHandlerImpl(ctx.getReqMsg());
    	if (incommingMessageHandler != null) {
    		try {
    			incommingMessageHandler.handle(ctx);
			} catch (Exception e) {
				// general error
	    		ctx.setResponseCode(InternalRC.GENERAL_ERROR);
			}
    	
    	} else {
    		// unknown message handler
    		ctx.setResponseCode(InternalRC.INVALID_MESSAGE);
    	}
    	
    	log.info("Sending Response [{}]", ctx.getRespMsg().dumpField(false, 
    			ctx.getConnectionId(), ctx.getClientIpAddress(), 
    			ctx.getClientPort(), System.currentTimeMillis()));
        return ctx.getRespMsg().writeData();
    }

}
