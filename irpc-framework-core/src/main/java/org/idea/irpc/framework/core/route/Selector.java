package org.idea.irpc.framework.core.route;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.idea.irpc.framework.core.common.ChannelFutureWrapper;

import java.util.List;

/**
 * @author Cheng Yang
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Selector {
    /**
     * 服务名称, 类的权限定名, 如 {@code com.idea.cyan.service.DataService}
     */
    private String providerServiceName;
    private List<ChannelFutureWrapper> channelFutureWrappers;

    public Selector(String providerServiceName) {
        this.providerServiceName = providerServiceName;
    }
}
