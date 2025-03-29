package com.codeinvocation.middleware.config;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.MessageTimeoutException;
import org.springframework.integration.aggregator.AggregatingMessageHandler;
import org.springframework.integration.aggregator.CorrelationStrategy;
import org.springframework.integration.aggregator.DefaultAggregatingMessageGroupProcessor;
import org.springframework.integration.aggregator.ReleaseStrategy;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableMessageHistory;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.expression.ValueExpression;
import org.springframework.integration.handler.BridgeHandler;
import org.springframework.integration.ip.tcp.TcpReceivingChannelAdapter;
import org.springframework.integration.ip.tcp.TcpSendingMessageHandler;
import org.springframework.integration.ip.tcp.connection.TcpNetClientConnectionFactory;
import org.springframework.integration.ip.tcp.serializer.AbstractByteArraySerializer;
import org.springframework.integration.router.AbstractMessageRouter;
import org.springframework.integration.store.MessageGroup;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.util.Assert;

import com.solab.iso8583.CustomIsoMessage;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.MessageFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableMessageHistory
public class DynamicTcpClient {

	@MessagingGateway(defaultRequestChannel = "toTcp.input")
	public interface ToTCP {
		public IsoMessage send(byte[] data, @Header("host") String host, @Header("port") int port);
	}
	
	@Bean
	public IntegrationFlow toTcp() {
		return f -> f.route(new TcpRouter());
	}

	public static class TcpRouter extends AbstractMessageRouter {

		private final static int MAX_CACHED = 10; // When this is exceeded, we remove the LRU.

		@SuppressWarnings("serial")
		private final LinkedHashMap<String, MessageChannel> subFlows =
				new LinkedHashMap<String, MessageChannel>(MAX_CACHED, .75f, true) {

					@Override
					protected boolean removeEldestEntry(Entry<String, MessageChannel> eldest) {
						if (size() > MAX_CACHED) {
							removeSubFlow(eldest);
							return true;
						}
						else {
							return false;
						}
					}

				};

		@Autowired
		private IntegrationFlowContext flowContext;

		@Autowired
		private AbstractByteArraySerializer asciiSerializer;
		
		@Override
		protected synchronized Collection<MessageChannel> determineTargetChannels(Message<?> message) {
			String hostPort = message.getHeaders().get("host", String.class) + message.getHeaders().get("port");
			log.info("determineTargetChannels {}", hostPort);
			MessageChannel channel = this.subFlows.get(hostPort);
			if (channel == null) {
				channel = createNewSubflow(message);
			}
			return Collections.singletonList(channel);
		}

		private MessageChannel createNewSubflow(Message<?> message) {
			String host = (String) message.getHeaders().get("host");
			Integer port = (Integer) message.getHeaders().get("port");
			Assert.state(host != null && port != null, "host and/or port header missing");
			String hostPort = host + port;
			log.info("createNewSubflow {}", hostPort);
			
			TcpNetClientConnectionFactory cf = new TcpNetClientConnectionFactory(host, port);
			cf.setSingleUse(false);
			cf.setSerializer(asciiSerializer);
			cf.setDeserializer(asciiSerializer);
			cf.setSoTimeout(180000);

			BridgeHandler bridge = new BridgeHandler();
		    bridge.setOrder(1);
		    bridge.setOutputChannelName("toAggregator.client");

			TcpSendingMessageHandler handler = new TcpSendingMessageHandler();
			handler.setConnectionFactory(cf);
			handler.setOrder(2);

			TcpReceivingChannelAdapter inboundChannelAdapter = new TcpReceivingChannelAdapter();
			inboundChannelAdapter.setConnectionFactory(cf);
			inboundChannelAdapter.setOutputChannelName("toAggregator.client");

			IntegrationFlow flow = f -> f
					.publishSubscribeChannel(Executors.newCachedThreadPool(), s -> s
								.subscribe(f1 -> f1.handle(bridge))
								.subscribe(f2 -> f2.handle(handler))
							);
			
			IntegrationFlowContext.IntegrationFlowRegistration flowRegistration =
					this.flowContext.registration(flow)
							.addBean(cf)
							.addBean(inboundChannelAdapter)
							.id(hostPort + ".flow")
							.register();
			MessageChannel inputChannel = flowRegistration.getInputChannel();
			this.subFlows.put(hostPort, inputChannel);
			return inputChannel;
		}

		private void removeSubFlow(Entry<String, MessageChannel> eldest) {
			String hostPort = eldest.getKey();
			this.flowContext.remove(hostPort + ".flow");
		}
		
	}

	@Autowired
	private MessageFactory<CustomIsoMessage> messageFactory;
	
	@Bean("toAggregator.client")
	public MessageChannel toAggregatorClient() {
		return new DirectChannel();
	}
	
	@Bean
	@ServiceActivator(inputChannel = "toAggregator.client")
	public MessageHandler aggregator() {
		AggregatingMessageHandler aggregator = new AggregatingMessageHandler(new DefaultAggregatingMessageGroupProcessor());
	    aggregator.setOutputChannelName("toTransformer.client");
	    aggregator.setExpireGroupsUponCompletion(true);
	    aggregator.setExpireGroupsUponTimeout(true);
	    aggregator.setDiscardChannelName("noResponseChannel");
	    aggregator.setGroupTimeoutExpression(new ValueExpression<>(180000L));
	    aggregator.setCorrelationStrategy(new CorrelationStrategy() {			
			@Override
			public Object getCorrelationKey(Message<?> message) {
	
				Object payload = message.getPayload();
				try {
					CustomIsoMessage isoMsg = messageFactory.parseMessage(((byte[])payload), 0);
					String terminalId = isoMsg.getObjectValue(41);
					String stan = isoMsg.getObjectValue(11);
					String remoteHost = message.getHeaders().get("ip_address", String.class);
					Integer remotePort = message.getHeaders().get("ip_tcp_remotePort", Integer.class);
					
					remoteHost = StringUtils.defaultIfBlank(remoteHost, message.getHeaders().get("host", String.class));
					remotePort = remotePort != null ? remotePort : message.getHeaders().get("port", Integer.class);
					
					boolean imcomming = !message.getHeaders().containsKey("replyChannel");
					String msgLog = isoMsg.dumpField(imcomming, 
							terminalId+stan, remoteHost, remotePort, 
							Long.valueOf(message.getHeaders().get("timestamp").toString()));
					log.info(imcomming? "Receive Response [{}]" : "Sending Request [{}]", msgLog);
					return remoteHost+remotePort+terminalId+stan;
					
				} catch (Exception e) {
					log.error("aggregator error", e);
				}
				return payload;
			}
	    });
	    
	    aggregator.setReleaseStrategy(new ReleaseStrategy() {			
			@Override
			public boolean canRelease(MessageGroup group) {
				return group.getMessages().size() == 2;
			}
		});
	    return aggregator;
	}
	
	@Transformer(inputChannel="toTransformer.client")
    public CustomIsoMessage convert(@Payload GenericMessage<List<byte[]>> messages) {
		try {
			return messageFactory.parseMessage(messages.getPayload().get(1), 0);
		} catch (Exception e) {
			log.error("ERROR WHEN TRANSFORM MESSAGE [toTransformer.client]", e);
		}
		return null;
    }
	
	@Bean("noResponseChannel")
	public MessageChannel noResponseChannel() {
		return new DirectChannel();
	}
	
	@ServiceActivator(inputChannel="noResponseChannel")
    public MessageTimeoutException handleTimeout(String input) throws Exception {    	
		return new MessageTimeoutException("No response received for " + input);
    }
	
}
