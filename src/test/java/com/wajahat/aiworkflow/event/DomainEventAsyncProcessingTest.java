package com.wajahat.aiworkflow.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.wajahat.aiworkflow.config.AsyncConfig;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(classes = {
        AsyncConfig.class,
        DomainEventListener.class,
        DomainEventAsyncProcessingTest.TestConfig.class
})
class DomainEventAsyncProcessingTest {

    private static final CountDownLatch handled = new CountDownLatch(1);
    private static final AtomicInteger nonMatchingHandlerCalls = new AtomicInteger();
    private static final AtomicReference<String> handlerThreadName = new AtomicReference<>();

    @Autowired
    private org.springframework.context.ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setUp() {
        nonMatchingHandlerCalls.set(0);
        handlerThreadName.set(null);
    }

    @Test
    void springDomainEventShouldProcessMatchingHandlersOnDomainEventExecutor() throws Exception {
        DomainEvent event = DomainEvent.of(
                DomainEventType.WORKFLOW_STARTED,
                UUID.randomUUID(),
                "WorkflowRun",
                Map.of()
        );

        applicationEventPublisher.publishEvent(event);

        assertThat(handled.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(handlerThreadName.get()).startsWith("domain-event-");
        assertThat(nonMatchingHandlerCalls.get()).isZero();
    }

    @Configuration
    static class TestConfig {

        @Bean
        DomainEventHandler matchingHandler() {
            return new DomainEventHandler() {
                @Override
                public boolean supports(DomainEventType type) {
                    return type == DomainEventType.WORKFLOW_STARTED;
                }

                @Override
                public void handle(DomainEvent event) {
                    handlerThreadName.set(Thread.currentThread().getName());
                    handled.countDown();
                }
            };
        }

        @Bean
        DomainEventHandler nonMatchingHandler() {
            return new DomainEventHandler() {
                @Override
                public boolean supports(DomainEventType type) {
                    return false;
                }

                @Override
                public void handle(DomainEvent event) {
                    nonMatchingHandlerCalls.incrementAndGet();
                }
            };
        }
    }
}
