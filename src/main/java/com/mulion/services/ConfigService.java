package com.mulion.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigService {
    private static final ConfigService configService = new ConfigService();
    private final Properties properties;

    private ConfigService() {
        properties = new Properties();

        try (InputStream input = new FileInputStream("./src/main/resources/config.properties")) {
            properties.load(input);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public static String getProperty(String property) {
        return configService.properties.getProperty(property);
    }
}
