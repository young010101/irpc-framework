package org.idea.irpc.framework.core.registy.zookeeper;


/**
 * @author cyang
 */
public class ProviderNodeInfo {
    private String serviceName;
    private String address;

    public void setAddress(String address) {
        this.address = address;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
