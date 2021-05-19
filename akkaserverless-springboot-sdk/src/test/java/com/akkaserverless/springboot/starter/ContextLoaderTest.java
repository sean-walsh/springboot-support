package com.akkaserverless.springboot.starter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.akkaserverless.javasdk.AkkaServerless;
import com.akkaserverless.springboot.starter.autoconfigure.AkkaServerlessAutoConfiguration;

import static junit.framework.TestCase.assertNotNull;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AkkaServerlessAutoConfiguration.class)
public class ContextLoaderTest {

    @Autowired
    private AkkaServerless akkaServerless;

    @Test
    public void akkaServerless_NotBeNull(){
        assertNotNull(akkaServerless);
    }
}
