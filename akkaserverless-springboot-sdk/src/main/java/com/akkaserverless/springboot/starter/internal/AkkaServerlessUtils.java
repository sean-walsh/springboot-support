package com.akkaserverless.springboot.starter.internal;

import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntityOptions;
import com.akkaserverless.javasdk.replicatedentity.ReplicatedEntityOptions;
import com.google.protobuf.Descriptors;
import com.akkaserverless.javasdk.AkkaServerless;
import com.akkaserverless.javasdk.Context;
import com.akkaserverless.javasdk.EntityFactory;
import com.akkaserverless.javasdk.EntityId;
import com.akkaserverless.javasdk.eventsourcedentity.EventSourcedEntity;
import com.akkaserverless.javasdk.impl.AnySupport;
import com.akkaserverless.javasdk.impl.replicatedentity.AnnotationBasedReplicatedEntitySupport;
import com.akkaserverless.javasdk.impl.eventsourcedentity.AnnotationBasedEventSourcedSupport;
import com.akkaserverless.springboot.starter.AkkaServerlessContext;
import com.akkaserverless.springboot.starter.autoconfigure.AkkaServerlessProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;
import java.util.*;

public final class AkkaServerlessUtils {
    private static final Logger LOG = LoggerFactory.getLogger(AkkaServerlessUtils.class);
    public static final String AKKASERVERLESS_SPRINGBOOT_SUPPORT = "akkaserverless-springboot-support";

    public static AkkaServerless register(AkkaServerless akkaServerless, ApplicationContext applicationContext, AkkaServerlessEntityScan entityScan) throws Exception {
        // Setting environments before create AkkaServerless server
        setServerOptions(entityScan);
        final List<Entity> entities = entityScan.findEntities();

        if (Objects.nonNull(entities) && !entities.isEmpty()){

            entities.forEach(entity -> {
                EntityFactory entitySupportFactory = new BaseEntitySupportFactory(entity, applicationContext);
                Class<?> entityClass = entitySupportFactory.entityClass();
                final AnySupport anySupport = newAnySupport(entity.getAdditionalDescriptors());

                if (Objects.nonNull(entity.getDescriptor())) {
                    switch (entity.getEntityType()) {
                        case EventSourcedEntity:
                            akkaServerless.registerEventSourcedEntity(
                                    new AnnotationBasedEventSourcedSupport(entitySupportFactory, anySupport, entity.getDescriptor()),
                                    entity.getDescriptor(),
                                    getPersistenceId(entityClass),
                                    getSnapshotEvery(entityClass),
                                    EventSourcedEntityOptions.defaults(),
                                    entity.getAdditionalDescriptors()
                            );

                            break;
                        case ReplicatedEntity:
                            akkaServerless.registerReplicatedEntity(
                                    new AnnotationBasedReplicatedEntitySupport(entitySupportFactory, anySupport, entity.getDescriptor()),
                                    entity.getDescriptor(),
                                    ReplicatedEntityOptions.defaults(),
                                    entity.getAdditionalDescriptors());

                            break;
                        default:
                            throw new IllegalArgumentException(
                                    String.format("Unknown entity type %s", entity.getEntityType()));
                    }
                } else {
                    LOG.warn("Entity '{}' was found but no valid ServiceDescriptor was declared",
                            entity.getEntityClass().getName());
                }
            });
        }
        return akkaServerless;
    }

    public static Object postConstructObject(Object obj, Context eventSourcedEntityCreationContext, String entityId){
        final Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field: fields){
            LOG.trace("Field: {}", field);
            setEntityId(entityId, obj, field);
            setAkkaServerlessContext(eventSourcedEntityCreationContext, obj, field);
        }

        return obj;
    }

    public static void setEntityId(String entityId, Object obj, Field field) {
        if (field.isAnnotationPresent(EntityId.class)) {
            field.setAccessible(true);
            try {
                if (field.getType().equals(String.class)) {
                    LOG.debug("Set the EntityId: {}", entityId);
                    field.set(obj, entityId);
                } else {
                    LOG.warn("Type of Field annotated with @EntityId must be String.class");
                }
            } catch (IllegalAccessException e) {
                LOG.error("");
                e.printStackTrace();
            }
        }
    }

    public static void setAkkaServerlessContext(Context eventSourcedEntityCreationContext, Object obj, Field field) {
        if (field.isAnnotationPresent(AkkaServerlessContext.class) && Objects.nonNull(eventSourcedEntityCreationContext)) {
            field.setAccessible(true);
            try {
                LOG.debug("Set the EventSourcedEntityCreationContext: {}", eventSourcedEntityCreationContext);
                field.set(obj, eventSourcedEntityCreationContext);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getPersistenceId(Class<?> entityClass) {
        EventSourcedEntity ann = entityClass.getAnnotation(EventSourcedEntity.class);
        String p = Optional.ofNullable(ann.entityType()).orElse("");
        return ( p.trim().isEmpty() ? entityClass.getSimpleName() : p );
    }

    private static int getSnapshotEvery(Class<?> entityClass) {
        EventSourcedEntity ann = entityClass.getAnnotation(EventSourcedEntity.class);
        return ann.snapshotEvery();
    }

    private static AnySupport newAnySupport(Descriptors.FileDescriptor[] descriptors) {
        return new AnySupport(
                descriptors,
                AkkaServerlessUtils.class.getClassLoader(),
                AnySupport.DefaultTypeUrlPrefix(),
                AnySupport.PREFER_JAVA());
    }

    /**
     * TODO SAW--
     */
    private static void setServerOptions(AkkaServerlessEntityScan entityScan) throws Exception {
        // This is workaround to 0.4.3 java-support.
        // In upcoming releases this should be resolved via HOCON config instance directly
        AkkaServerlessProperties properties = entityScan.getProperties();
        Map<String, String> props = new HashMap<>();

        if (!properties.USER_FUNCTION_INTERFACE_DEFAULT.equals(properties.getUserFunctionInterface())) {
            props.put("HOST", properties.getUserFunctionInterface());
        }

        if (properties.USER_FUNCTION_PORT != properties.getUserFunctionPort()) {
            props.put("PORT", String.valueOf(properties.getUserFunctionPort()));
        }

        if (!props.isEmpty()) {
            props.put("SUPPORT_LIBRARY_NAME", AKKASERVERLESS_SPRINGBOOT_SUPPORT);
            setEnv(Collections.unmodifiableMap(props));
        }
    }

    private static void setEnv(Map<String, String> newenv) throws Exception {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>)     theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        } catch (NoSuchFieldException e) {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for(Class cl : classes) {
                if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.clear();
                    map.putAll(newenv);
                }
            }
        }
    }
}
