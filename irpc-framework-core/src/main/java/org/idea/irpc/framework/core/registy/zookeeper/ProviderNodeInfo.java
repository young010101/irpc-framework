package org.idea.irpc.framework.core.registy.zookeeper;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author cyang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderNodeInfo {
    private String serviceName;
    private String address;
    private String registryTime;
    private Integer weight;
}
