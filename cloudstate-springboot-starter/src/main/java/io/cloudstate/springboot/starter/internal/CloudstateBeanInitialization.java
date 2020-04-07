package io.cloudstate.springboot.starter.internal;

import akka.Done;
import io.cloudstate.javasupport.CloudState;
import io.cloudstate.springboot.starter.autoconfigure.CloudstateProperties;
import io.cloudstate.springboot.starter.internal.scan.CloudstateEntityScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import static io.cloudstate.springboot.starter.internal.CloudstateUtils.register;

@Component
public final class CloudstateBeanInitialization {
    private static final Logger log = LoggerFactory.getLogger(CloudstateBeanInitialization.class);

    private final CloudState cloudState;
    private final ApplicationContext context;
    private final CloudstateEntityScan entityScan;
    private final CloudstateProperties properties;
    private final ThreadLocal<Map<Class<?>, Map<String, Object>>> stateController;

    private static ExecutorService workerThreadService =
            Executors.newFixedThreadPool(1, new CustomizableThreadFactory("cloudstate-t"));

    @Autowired
    public CloudstateBeanInitialization(
            ApplicationContext context,
            ThreadLocal<Map<Class<?>, Map<String, Object>>> stateController,
            CloudstateEntityScan entityScan,
            CloudState cloudState,
            CloudstateProperties properties) {
        this.context = context;
        this.stateController = stateController;
        this.cloudState = cloudState;
        this.entityScan = entityScan;
        this.properties = properties;
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Runnable worker = () -> {
            final Instant start = Instant.now();
            log.info("Starting Cloudstate Server...");
            try {
                if (isAutoRegister()) {
                    register(cloudState, stateController, context, entityScan, properties);
                }

                cloudState
                        .start()
                        .toCompletableFuture()
                        .exceptionally(ex -> {
                            log.error("Failure on Cloudstate Server startup", ex);
                            return Done.done();
                        }).thenAccept(done -> {
                    Duration timeElapsed = Duration.between(start, Instant.now());
                    log.info("Cloudstate Server keep alive for {}", timeElapsed);
                }).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        workerThreadService.execute(worker);
    }

    @EventListener
    public void onApplicationEvent(ContextClosedEvent event) {
        if (!workerThreadService.isShutdown() || !workerThreadService.isTerminated()) {
            workerThreadService.shutdown();
        }
    }

    private boolean isAutoRegister() {
        return Objects.nonNull(properties) && properties.isAutoRegister();
    }

}