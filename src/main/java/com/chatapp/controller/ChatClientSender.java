package com.chatapp.controller;

import com.chatapp.model.Message;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ChatClientSender {
    private final Properties props;
    private final String topic;
    private static final String KEY = "chat";
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatClientSender.class);

    public ChatClientSender(Properties defaultProps, String topic) {
        props = new Properties();
        props.putAll(defaultProps);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaJsonSerializer");
        this.topic = topic;
    }

    public void send(Message message) {
        Producer<String, Message> producer = new KafkaProducer<>(props);
        ProducerRecord<String, Message> record = new ProducerRecord<>(topic, KEY, message);
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                LOGGER.error(exception.getMessage(), exception);
            } else {
                LOGGER.debug("Sent message \"{}\", from user \"{}\"." +
                             " Produced record to topic {} partition [{}] @ offset {}",
                        message.getText(), message.getNickname(),
                        metadata.topic(), metadata.partition(), metadata.offset());
            }
        });
    }

    public Properties getProps() {
        return props;
    }

    public String getTopic() {
        return topic;
    }
}
