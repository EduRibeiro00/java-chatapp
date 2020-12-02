package com.chatapp.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class KafkaPropertiesLoader {

    public static Properties loadProperties() throws IOException {

        Properties properties = new Properties();
        try (InputStream is = KafkaPropertiesLoader.class.getClassLoader().getResourceAsStream("kafka.config")) {
            properties.load(is);
        }
        return properties;
    }
}
