package io.twentysixty.ollama.hologram.chatbot.jms;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import io.github.ollama4j.models.chat.OllamaChatMessage;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.twentysixty.ollama.hologram.chatbot.model.LlamaRole;
import io.twentysixty.ollama.hologram.chatbot.svc.OllamaService;
import io.twentysixty.sa.client.jms.AbstractConsumer;
import io.twentysixty.sa.client.jms.ConsumerInterface;
import io.twentysixty.sa.client.model.message.BaseMessage;
import io.twentysixty.sa.client.model.message.TextMessage;
import io.twentysixty.sa.res.c.MessageResource;

@ApplicationScoped
public class OolamaConsumer extends AbstractConsumer<UUID> implements ConsumerInterface<UUID> {

	@Inject
	OllamaService service;
	
	@Inject
	MtProducer mtProducer;
	

	@Inject
    ConnectionFactory _connectionFactory;

	
	@ConfigProperty(name = "io.twentysixty.demos.auth.jms.ex.delay")
	Long _exDelay;
	
	
	@ConfigProperty(name = "io.twentysixty.demos.auth.jms.ollamamt.queue.name")
	String _queueName;
	
	@ConfigProperty(name = "io.twentysixty.demos.auth.jms.ollamamt.consumer.threads")
	Integer _threads;
	
	@ConfigProperty(name = "io.twentysixty.demos.auth.debug")
	Boolean _debug;
	
	
	private static final Logger logger = Logger.getLogger(OolamaConsumer.class);

	
	
	void onStart(@Observes StartupEvent ev) {
    	
		logger.info("onStart: BeConsumer queueName: " + _queueName);
		
		this.setExDelay(_exDelay);
		this.setDebug(_debug);
		this.setQueueName(_queueName);
		this.setThreads(_threads);
		this.setConnectionFactory(_connectionFactory);
		super._onStart();
		
    }

    void onStop(@Observes ShutdownEvent ev) {
    	
    	logger.info("onStop: BeConsumer");
		
    	
    	super._onStop();
    	
    }
	
    @Override
	public void receiveMessage(UUID uuid) throws Exception {
		
    	List<OllamaChatMessage> messages = service.getMessagesFromHistory(uuid);
    	String response = service.getChatResponse(messages);
    	service.historize(uuid, LlamaRole.ASSISTANT, response);
    	TextMessage tm = TextMessage.build(uuid, null, response);
    	mtProducer.sendMessage(tm);
		
	}

	
}
