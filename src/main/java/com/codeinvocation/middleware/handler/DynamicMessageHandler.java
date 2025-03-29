package com.codeinvocation.middleware.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codeinvocation.middleware.constant.MTI;
import com.solab.iso8583.IsoMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DynamicMessageHandler {
	
	private final Map<String, IncommingMessageHandler> messageHandlers = new HashMap<String, IncommingMessageHandler>();
	
	@Autowired
	public DynamicMessageHandler(List<IncommingMessageHandler> incommingMessageHandlers) {
		for (IncommingMessageHandler messageHandlers : incommingMessageHandlers) {
			String[] keys = messageHandlers.keys();
			for (String key : keys) {
				this.messageHandlers.put(key, messageHandlers);
			}
		}
	}
	
	public IncommingMessageHandler getMessageHandlerImpl(IsoMessage reqMsg) {
		int type = reqMsg.getType();
    	String handlerKey = "";
		
    	if (type == MTI.NETWORK_MANAGEMENT.val) {
    		String networkManagement = reqMsg.getObjectValue(70);
	    	handlerKey = StringUtils.join(
	    			new String[] {MTI.NETWORK_MANAGEMENT.getString(), networkManagement}, 
	    			".");
	    	
    	} else if (type == MTI.TRANSACTIONAL.val) {
    		String processingCode = reqMsg.getObjectValue(3);
	    	handlerKey = StringUtils.join(
	    			new String[] {MTI.TRANSACTIONAL.getString(), processingCode}, 
	    			".");
			
    	} else if (type == MTI.ADVICE.val) {
    		handlerKey = MTI.ADVICE.getString();
    		
    	} else if (type == MTI.ADVICE_REPEAT.val) {
    		handlerKey = MTI.ADVICE_REPEAT.getString();
    		
    	} else if (type == MTI.REVERSE.val) {
    		handlerKey = MTI.REVERSE.getString();
    		
    	} else {
    		log.warn("Unknown Message Handler {}", handlerKey);
    	}
    	
    	log.info("Message Handler {}", handlerKey);
		Set<String> keySet = this.messageHandlers.keySet();
		for (String keyService : keySet) {
			if (StringUtils.equalsAnyIgnoreCase(handlerKey, keyService)) {
				return this.messageHandlers.get(keyService);
			}
		}
		return null;
	}
	
	
}
