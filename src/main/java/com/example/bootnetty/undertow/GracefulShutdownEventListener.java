package com.example.bootnetty.undertow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GracefulShutdownEventListener implements ApplicationListener<ContextClosedEvent> {
    private final GracefulShutdownHandlerWrapper gracefulShutdownHandlerWrapper;

    public GracefulShutdownEventListener(GracefulShutdownHandlerWrapper gracefulShutdownHandlerWrapper) {
        this.gracefulShutdownHandlerWrapper = gracefulShutdownHandlerWrapper;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {

        log.info("GRACEFUL_SHUTDOWN_STARTED");

        // 이 시점부터 새로운 요청이 거부된다. 클라이언트는 503 Service Unavailable 응답을 수신한다.
        gracefulShutdownHandlerWrapper.getGracefulShutdownHandler().shutdown();

        try {
            // 이 시점에 기존 처리 중인 요청에 대한 응답을 완료한다.
            gracefulShutdownHandlerWrapper.getGracefulShutdownHandler().awaitShutdown();
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("GRACEFUL_SHUTDOWN_FAILED");
        }

        log.info("GRACEFUL_SHUTDOWN_FINISHED");
    }
}
