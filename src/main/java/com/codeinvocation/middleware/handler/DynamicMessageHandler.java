package com.codeinvocation.middleware.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.solab.iso8583.IsoMessage;

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
    	String mti = String.format("%04x", type);
    	String processingCode = reqMsg.getObjectValue(3);
    	String networkManagement = reqMsg.getObjectValue(70);
    	String reqMsgKey = StringUtils.join(new String[] {mti, processingCode, networkManagement}, ".");
    	
		Set<String> keySet = this.messageHandlers.keySet();
		for (String keyService : keySet) {
			if (StringUtils.equalsAnyIgnoreCase(reqMsgKey, keyService)) {
				return this.messageHandlers.get(keyService);
			}
		}
		return null;
	}
	
	
}
