package com.codeinvocation.middleware.dto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.codeinvocation.middleware.constant.InternalRC;
import com.solab.iso8583.CustomIsoMessage;
import com.solab.iso8583.IsoType;

import lombok.Builder;

@Builder
public class TransactionContext {
	private byte[] rawReqMsg;
	private CustomIsoMessage reqMsg;
	private CustomIsoMessage respMsg;
	private String clientIpAddress;
	private Integer clientPort;
	private String connectionId;
	private Long requestTimestamp;
	private Long responseTimestamp;
	@Builder.Default
	private Map<ContextKey, Object> map = Collections.synchronizedMap(new HashMap<ContextKey, Object>());
	
	public void setResponseCode(InternalRC internalRc) {
		respMsg.setValue(39, internalRc.val, IsoType.ALPHA, 2);
	}
	
	public <T> void put(ContextKey key, T obj) {
		map.put(key, obj);
	}

	public <T> void get(ContextKey key) {
		map.get(key);
	}
	
	public byte[] getRawReqMsg() {
		return rawReqMsg;
	}
	public void setRawReqMsg(byte[] rawReqMsg) {
		this.rawReqMsg = rawReqMsg;
	}
	public CustomIsoMessage getReqMsg() {
		return reqMsg;
	}
	public void setReqMsg(CustomIsoMessage reqMsg) {
		this.reqMsg = reqMsg;
	}
	public CustomIsoMessage getRespMsg() {
		return respMsg;
	}
	public void setRespMsg(CustomIsoMessage respMsg) {
		this.respMsg = respMsg;
	}
	public String getClientIpAddress() {
		return clientIpAddress;
	}
	public void setClientIpAddress(String clientIpAddress) {
		this.clientIpAddress = clientIpAddress;
	}
	public Integer getClientPort() {
		return clientPort;
	}
	public void setClientPort(Integer clientPort) {
		this.clientPort = clientPort;
	}
	public String getConnectionId() {
		return connectionId;
	}
	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}
	public Long getRequestTimestamp() {
		return requestTimestamp;
	}
	public void setRequestTimestamp(Long requestTimestamp) {
		this.requestTimestamp = requestTimestamp;
	}

	public Long getResponseTimestamp() {
		return responseTimestamp;
	}

	public void setResponseTimestamp(Long responseTimestamp) {
		this.responseTimestamp = responseTimestamp;
	}
	
}
