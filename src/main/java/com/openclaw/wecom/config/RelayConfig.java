package com.openclaw.wecom.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wecom.relay")
public class RelayConfig {

    private int responseTimeout = 60000;

    private int pingInterval = 30000;

    private int maxPendingMessages = 1000;
}
