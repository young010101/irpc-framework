package org.idea.irpc.framework.core.common;

import io.netty.channel.ChannelFuture;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * @author cyang
 */
@Data
@RequiredArgsConstructor
public class ChannelFutureWrapper {
    private final String host;
    private final int port;
    private ChannelFuture channelFuture;
}
