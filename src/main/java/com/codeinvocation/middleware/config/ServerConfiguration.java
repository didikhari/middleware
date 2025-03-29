package com.codeinvocation.middleware.config;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.ip.tcp.TcpInboundGateway;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNioServerConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.AbstractByteArraySerializer;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.annotation.EnableAsync;

import com.codeinvocation.middleware.serializer.ASCIISerializer;
import com.solab.iso8583.CustomIsoMessage;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.parse.ConfigParser;


@EnableAsync
@Configuration
@EnableCaching
@EnableIntegration
@IntegrationComponentScan
public class ServerConfiguration {

	@Bean
	public MessageFactory<CustomIsoMessage> messageFactory() throws IOException {
		MessageFactory<CustomIsoMessage> messageFactory = new MessageFactory<CustomIsoMessage>() {
			@Override
			protected CustomIsoMessage createIsoMessage(String header) {
				return new CustomIsoMessage(header);
			}
		};
		messageFactory.setAssignDate(true);
		ConfigParser.configureFromUrl(messageFactory, new File("config/j8583.xml").toURI().toURL());
		return messageFactory;
	}
	
	/**************************************
	 * 
	 * SERVER CONFIGURATION
	 * 
	 **************************************/

	@Bean
	@Qualifier(value = "ASCIIServerCF")
    public AbstractServerConnectionFactory serverASCII_CF(@Value("${ascii.server.port}") int port, 
    		AbstractByteArraySerializer asciiSerializer) { 
		TcpNioServerConnectionFactory connectionFactory = new TcpNioServerConnectionFactory(port);
		connectionFactory.setSerializer(asciiSerializer);
		connectionFactory.setDeserializer(asciiSerializer);
        return connectionFactory;
    }

    @Bean
    public TcpInboundGateway tcpASCIIInGate(@Qualifier("ASCIIServerCF") AbstractServerConnectionFactory connectionFactory)  {
        TcpInboundGateway inGate = new TcpInboundGateway();
        inGate.setConnectionFactory(connectionFactory);
        inGate.setRequestChannel(fromTcp());
        return inGate;
    }

	@Bean
    public MessageChannel fromTcp() {
        return new DirectChannel();
    }
	
	@Bean
	public AbstractByteArraySerializer asciiSerializer() {
		return new ASCIISerializer(false);
	}
	
	@Bean
	public AtomicInteger stan() {
		return new AtomicInteger();
	}
}
