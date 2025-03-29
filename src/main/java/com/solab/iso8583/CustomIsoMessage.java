package com.solab.iso8583;

import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;

public class CustomIsoMessage extends IsoMessage {
	
	public CustomIsoMessage(String header) {
		super(header);
	}
	
	public CustomIsoMessage() {
		super();
	}
	
	public String getString(int bitNo) {
		Object result = this.getObjectValue(bitNo);
		if (result == null)
			return null;
		
		if (result instanceof String)
			return (String) result;
		else
			return result.toString();
	}
	
	public String getTransmissionDateTime() {
		Date bit7Date = this.getObjectValue(7);
		return DateFormatUtils.format(bit7Date, "MMddHHmmss");
	}
	
	public Integer getInteger(int bitNo) {
		Object result = this.getObjectValue(bitNo);
		if (result == null)
			return null;
		
		if (result instanceof Integer)
			return (Integer) result;
		else
			return Integer.valueOf(result.toString());
	}
	
	public String dumpField(boolean incoming, String connectionId, String clientIpAddress, Integer clientPort,
			Long timestamp) {
		String direction = incoming ? "INCOMING":"OUTGOING";
        StringBuilder sb = new StringBuilder("\n");
        sb.append("====="+direction+" ISOMSG=====\n");
        sb.append("CONNECTION ID\t"+connectionId);
        sb.append("\n");
        sb.append("REMOTE ADDRESS\t"+clientIpAddress+":"+clientPort);
        sb.append("\n");
        sb.append("TIMESTAMP\t"+DateFormatUtils.format(timestamp, "yyyy-MM-dd'T'HH:mm:ss.ssZ"));
        sb.append("\n");
        sb.append(String.format("MTI\t[%04x]", getType()));
        sb.append("\n");

        //Fields
        for (int i = 2; i < 129; i++) {
            IsoValue<?> v = getField(i);
            if (v != null) {
            	sb.append("DE-"+i);
                String desc = v.toString();
                sb.append("\t[");
                sb.append(desc);
                sb.append("]\n");
            }
        }

        sb.append("====="+direction+" ISOMSG=====\n");
        return sb.toString();
    
	}
}
