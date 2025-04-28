package org.idea.irpc.framework.core.registy.zookeeper;


import lombok.Data;

/**
 * @author cyang
 */
@Data
public class ProviderNodeInfo {
    private String serviceName;
    private String address;
}
