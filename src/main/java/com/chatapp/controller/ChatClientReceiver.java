package com.chatapp.controller;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class ChatClientReceiver {
    private final AtomicBoolean consuming = new AtomicBoolean(true);
    private final Properties props;
    private final Consumer<String, String> consumer;
    private static final Duration WAITING_DURATION = Duration.ofMillis(100);
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatClientReceiver.class);

    public ChatClientReceiver(Properties defaultProps) {
        props = new Properties();
        props.putAll(defaultProps);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumer = new KafkaConsumer<>(props);

        Runtime.getRuntime().addShutdownHook((new Thread(() -> {
            System.out.println("Terminating receiver...");
            consuming.set(false);
            consumer.wakeup();
            System.out.println("Receiver shut down.");
        })));
    }

    public void poll(final String topic, final BiConsumer<LocalDateTime, String> callback) {

        try (consumer) {
            consumer.subscribe(Collections.singletonList(topic));
            while (consuming.get()) {
                ConsumerRecords<String, String> records = consumer.poll(WAITING_DURATION);
                for (ConsumerRecord<String, String> record : records) {
                    LOGGER.debug("[{}] Received record {}",
                            record.timestamp(),
                            record.value()
                    );
                    callback.accept(
                            Instant.ofEpochMilli(record.timestamp()).
                                    atZone(ZoneId.systemDefault()).
                                    toLocalDateTime(),
                            record.value()
                    );
                }
            }
        } catch (WakeupException e) {
            if (consuming.get()) throw e;
        }
    }

    public Properties getProps() {
        return props;
    }

    public Consumer<String, String> getConsumer() {
        return consumer;
    }
}
