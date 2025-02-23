package com.codeinvocation.middleware.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.ip.tcp.TcpInboundGateway;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNioServerConnectionFactory;
import org.springframework.messaging.MessageChannel;

import com.codeinvocation.middleware.serializer.ASCIISerializer;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;


@Configuration
public class BillerSimulatorConfiguration {

	@Bean
	public MessageFactory<IsoMessage> messageFactory() throws IOException {
		MessageFactory<IsoMessage> messageFactory = new MessageFactory<IsoMessage>();
		messageFactory.setConfigPath("j8583.xml");
		messageFactory.setAssignDate(true);
		return messageFactory;
	}
	
	/**************************************
	 * 
	 * SERVER CONFIGURATION
	 * 
	 **************************************/

	@Bean
	@Qualifier(value = "billerASCIIServerCF")
    public AbstractServerConnectionFactory serverASCII_CF(@Value("${biller.ascii.server.port}") int port) { 
		TcpNioServerConnectionFactory connectionFactory = new TcpNioServerConnectionFactory(port);
		connectionFactory.setSerializer(new ASCIISerializer(false));
		connectionFactory.setDeserializer(new ASCIISerializer(false));
        return connectionFactory;
    }

    @Bean
    public TcpInboundGateway tcpASCIIInGate(@Qualifier("billerASCIIServerCF") AbstractServerConnectionFactory connectionFactory)  {
        TcpInboundGateway inGate = new TcpInboundGateway();
        inGate.setConnectionFactory(connectionFactory);
        inGate.setRequestChannel(fromTcp());
        return inGate;
    }

	@Bean
    public MessageChannel fromTcp() {
        return new DirectChannel();
    }
	
}
