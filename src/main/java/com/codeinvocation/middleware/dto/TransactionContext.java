package com.codeinvocation.middleware.dto;

import com.codeinvocation.middleware.constant.InternalRC;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;

import lombok.Builder;

@Builder
public class TransactionContext {
	private byte[] rawReqMsg;
	private IsoMessage reqMsg;
	private IsoMessage respMsg;
	private String clientIpAddress;
	private Integer clientPort;
	private String connectionId;
	private Long requestTimestamp;
	
	public void setResponseCode(InternalRC internalRc) {
		respMsg.setValue(39, internalRc.val, IsoType.ALPHA, 2);
	}
	
	public byte[] getRawReqMsg() {
		return rawReqMsg;
	}
	public void setRawReqMsg(byte[] rawReqMsg) {
		this.rawReqMsg = rawReqMsg;
	}
	public IsoMessage getReqMsg() {
		return reqMsg;
	}
	public void setReqMsg(IsoMessage reqMsg) {
		this.reqMsg = reqMsg;
	}
	public IsoMessage getRespMsg() {
		return respMsg;
	}
	public void setRespMsg(IsoMessage respMsg) {
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
	
}
