package com.openclaw.wecom.service;

import com.openclaw.wecom.config.RelayConfig;
import com.openclaw.wecom.model.ClientMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PendingMessageManager {

    @Autowired
    private RelayConfig relayConfig;

    private final Map<String, CompletableFuture<ClientMessage>> pendingMessages = new ConcurrentHashMap<>();

    public CompletableFuture<ClientMessage> registerMessage(String messageId) {
        CompletableFuture<ClientMessage> future = new CompletableFuture<>();
        pendingMessages.put(messageId, future);

        future.orTimeout(relayConfig.getResponseTimeout(), TimeUnit.MILLISECONDS)
                .whenComplete((result, error) -> {
                    pendingMessages.remove(messageId);
                    if (error != null) {
                        log.warn("Message {} timed out or failed: {}", messageId, error.getMessage());
                    }
                });

        log.debug("Registered pending message: {}", messageId);
        return future;
    }

    public void completeMessage(String messageId, ClientMessage response) {
        CompletableFuture<ClientMessage> future = pendingMessages.remove(messageId);
        if (future != null) {
            future.complete(response);
            log.debug("Completed pending message: {}", messageId);
        } else {
            log.warn("No pending message found for id: {}", messageId);
        }
    }

    public void cancelMessage(String messageId) {
        CompletableFuture<ClientMessage> future = pendingMessages.remove(messageId);
        if (future != null) {
            future.cancel(true);
            log.debug("Cancelled pending message: {}", messageId);
        }
    }

    public int getPendingCount() {
        return pendingMessages.size();
    }
}
