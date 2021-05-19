package com.akkaserverless.springboot.starter.autoconfigure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.akkaserverless.springboot.starter.internal.AkkaServerlessEntityScan;
import com.akkaserverless.javasdk.AkkaServerless;

@Configuration
@ConditionalOnClass(AkkaServerless.class)
@EnableConfigurationProperties(AkkaServerlessProperties.class)
@ComponentScan(basePackages = "com.akkaserverless.springboot.starter")
public class AkkaServerlessAutoConfiguration {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private AkkaServerlessProperties akkaServerlessProperties;

    @Bean
    @ConditionalOnMissingBean
    public AkkaServerlessEntityScan akkaServerlessEntityScan() {
        return new AkkaServerlessEntityScan(context, akkaServerlessProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public AkkaServerless akkaServerless(AkkaServerlessEntityScan entityScan) throws Exception {
        return new AkkaServerless();
    }
}
