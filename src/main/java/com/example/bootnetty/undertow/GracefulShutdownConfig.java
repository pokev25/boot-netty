package com.example.bootnetty.undertow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GracefulShutdownConfig {

    private final GracefulShutdownHandlerWrapper gracefulShutdownHandlerWrapper;

    public GracefulShutdownConfig(GracefulShutdownHandlerWrapper gracefulShutdownHandlerWrapper) {
        this.gracefulShutdownHandlerWrapper = gracefulShutdownHandlerWrapper;
    }

    @Bean
    public UndertowServletWebServerFactory servletWebServerFactory() {

        UndertowServletWebServerFactory factory = new UndertowServletWebServerFactory();
        factory.addDeploymentInfoCustomizers(deploymentInfo -> deploymentInfo.addOuterHandlerChainWrapper(gracefulShutdownHandlerWrapper));

        return factory;
    }
}
