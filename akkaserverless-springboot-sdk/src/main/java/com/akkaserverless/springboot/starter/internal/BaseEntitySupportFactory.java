package com.akkaserverless.springboot.starter.internal;

import com.akkaserverless.javasdk.EntityContext;
import com.akkaserverless.javasdk.EntityFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import static com.akkaserverless.springboot.starter.internal.AkkaServerlessUtils.postConstructObject;

class BaseEntitySupportFactory implements EntityFactory {
    private static final Logger LOG = LoggerFactory.getLogger(AkkaServerlessUtils.class);

    private final Entity entity;
    private final ApplicationContext context;

    public BaseEntitySupportFactory(Entity entity, ApplicationContext context) {
        this.entity = entity;
        this.context = context;
    }

    @Override
    public Object create(EntityContext entityContext) {
        LOG.trace("Create instance of EventSourcedEntity");
        Object obj = context.getBean(entity.getEntityClass());
        return postConstructObject(obj, entityContext, entityContext.entityId());
    }

    @Override
    public Class<?> entityClass() {
        return entity.getEntityClass();
    }
}
