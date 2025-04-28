package org.idea.irpc.framework.core.service;

import lombok.Data;

/**
 * @author cyang
 */
@Data
public class ServerConfig {
    private String host;
    private int port;
    private String applicationName;
}
