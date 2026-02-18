package com.djelog.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "file:${user.dir}/.env", ignoreResourceNotFound = true)
@EnableConfigurationProperties
public class EnvConfig {
}
