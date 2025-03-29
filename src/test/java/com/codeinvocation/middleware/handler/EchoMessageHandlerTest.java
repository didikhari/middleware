package com.codeinvocation.middleware.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.codeinvocation.middleware.MiddlewareApplication;
import com.codeinvocation.middleware.config.DynamicTcpClient.ToTCP;
import com.solab.iso8583.CustomIsoMessage;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.MessageFactory;

@ActiveProfiles("test")
@SpringBootTest(classes = MiddlewareApplication.class)
class EchoMessageHandlerTest {

	@Autowired
	private ToTCP toTcp;
	
	@Autowired
	private MessageFactory<CustomIsoMessage> messageFactory;
	
	@Autowired
	private AtomicInteger stan;
	
	@Value("${ascii.server.port}")
	private Integer port;
	
	@Test
	void given_validMessage_when_sendingEcho_then_returnSuccess() {
		stan.compareAndSet(999999, 0);
		IsoMessage requestMsg = messageFactory.newMessage(0x800);
		requestMsg.setField(11, new IsoValue<Integer>(IsoType.NUMERIC, stan.addAndGet(1), 6));
		requestMsg.setField(41, new IsoValue<String>(IsoType.ALPHA, "12345678", 8));
		requestMsg.setField(70, new IsoValue<String>(IsoType.ALPHA, "301", 3));
		IsoMessage responseMsg = toTcp.send(requestMsg.writeData(), "127.0.0.1", port);
		assertNotNull(responseMsg);
		assertEquals("00", responseMsg.getObjectValue(39));
	}

}
