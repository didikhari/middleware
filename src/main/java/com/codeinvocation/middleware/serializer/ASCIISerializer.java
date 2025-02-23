package com.codeinvocation.middleware.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

import org.apache.commons.lang3.StringUtils;
import org.springframework.integration.ip.tcp.serializer.ByteArrayLengthHeaderSerializer;
import org.springframework.integration.ip.tcp.serializer.SoftEndOfStreamException;

public class ASCIISerializer extends ByteArrayLengthHeaderSerializer {
	private static final BigInteger ten = BigInteger.valueOf(10L);
	
	public ASCIISerializer(boolean headerIncluded) {
		super(HEADER_SIZE_INT);
		setInclusive(headerIncluded);
	}
	
	@Override
	protected int readHeader(InputStream inputStream) throws IOException {
		byte[] lengthPart = new byte[HEADER_SIZE_INT];
		int status = read(inputStream, lengthPart, true);
		if (status < 0)
			throw new SoftEndOfStreamException("Stream closed between payloads");
		return Integer.valueOf(new String(lengthPart));
	}

	@Override
	protected void writeHeader(OutputStream outputStream, int len) throws IOException {
		int maxLen = ten.pow(HEADER_SIZE_INT).intValue() - 1;       // 10^lengthDigits - 1

        if (len > maxLen)
            throw new IOException ("len exceeded ("+len+" > "+maxLen+")");
        else if (len < 0)
            throw new IOException ("invalid negative length ("+len+")");
        
        outputStream.write(StringUtils.leftPad(String.valueOf(len), HEADER_SIZE_INT, "0").getBytes());
	}
}
