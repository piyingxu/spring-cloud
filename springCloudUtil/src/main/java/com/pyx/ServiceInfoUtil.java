package com.pyx;

import org.apache.log4j.Logger;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Configuration
public class ServiceInfoUtil implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {
	
	private static Logger log = Logger.getLogger(ServiceInfoUtil.class);
	
    private static EmbeddedServletContainerInitializedEvent event;

    @Override
    public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
        ServiceInfoUtil.event = event;
    }

    @SuppressWarnings("deprecation")
	public static int getPort() {
        Assert.notNull(event);
        int port = event.getEmbeddedServletContainer().getPort();
        return port;
    }
 }