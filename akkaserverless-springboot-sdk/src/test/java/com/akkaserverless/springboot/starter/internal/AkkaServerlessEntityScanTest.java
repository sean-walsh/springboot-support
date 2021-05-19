package com.akkaserverless.springboot.starter.internal;

import com.akkaserverless.springboot.starter.autoconfigure.AkkaServerlessAutoConfiguration;
import com.akkaserverless.springboot.starter.autoconfigure.AkkaServerlessProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes= AkkaServerlessAutoConfiguration.class)
public class AkkaServerlessEntityScanTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private AkkaServerlessProperties akkaServerlessProperties;

    @Test
    public void findEntities_NotBeEmpty(){
        AkkaServerlessEntityScan scan = new AkkaServerlessEntityScan(context, akkaServerlessProperties);
        final List<Entity> entities = scan.findEntities();

        assertNotNull(entities);
        assertFalse(entities.isEmpty());
        assertEquals(2, entities.size());
    }
}
