package com.akkaserverless.springboot.starter.autoconfigure;

import akka.Done;
import com.akkaserverless.javasdk.AkkaServerless;
import com.akkaserverless.springboot.starter.internal.AkkaServerlessEntityScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

import static com.akkaserverless.springboot.starter.internal.AkkaServerlessUtils.register;

@Component
public class AkkaServerlessBeanInitialization {
    Logger log = LoggerFactory.getLogger(AkkaServerlessBeanInitialization.class);

    private final AkkaServerless akkaServerless;
    private final ApplicationContext context;
    private final AkkaServerlessEntityScan entityScan;

    @Autowired
    public AkkaServerlessBeanInitialization(ApplicationContext context, AkkaServerlessEntityScan entityScan,  AkkaServerless akkaServerless) {
        this.context = context;
        this.akkaServerless = akkaServerless;
        this.entityScan = entityScan;
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) throws Exception {
        final Instant start = Instant.now();
        register(akkaServerless, context, entityScan);
        log.info("Starting AkkaServerless Server...");
        akkaServerless.start()
                .toCompletableFuture()
                .exceptionally(ex -> {
                    log.error("Failure on AkkaServerless Server startup", ex);
                    return Done.done();
                }).thenAccept(done -> {
                    Duration timeElapsed = Duration.between(start, Instant.now());
                    log.debug("AkkaServerless Server keep alived for {}", timeElapsed);
                });
    }

}
