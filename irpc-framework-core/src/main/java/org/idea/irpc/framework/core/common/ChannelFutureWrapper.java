package org.idea.irpc.framework.core.common;

import io.netty.channel.ChannelFuture;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * netty {@code ChannelFuture}的包装类
 * @author cyang
 * @see ChannelFuture
 */
@Data
@RequiredArgsConstructor
public class ChannelFutureWrapper {
    private final String host;
    private final int port;
    private ChannelFuture channelFuture;
}
