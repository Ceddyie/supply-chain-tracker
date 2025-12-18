package de.hskl.trackingservice.config;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.outbound.PubSubMessageHandler;
import org.springframework.messaging.MessageHandler;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class PubSubConfig {
    @Value("${pubsub.topic.tracking-updates}")
    private String topicName;

    @Bean
    @ServiceActivator(inputChannel = "pubsubOutputChannel")
    public MessageHandler messageSender(PubSubTemplate pubSubTemplate) {
        return new PubSubMessageHandler(pubSubTemplate, topicName);
    }
}
