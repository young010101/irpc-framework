package org.idea.irpc.framework.core.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.common.constants.RpcConstants;

import java.util.List;

/**
 * @author cyang
 */
@Slf4j
public class RpcDecoder extends ByteToMessageDecoder {
    private static final int BASE_LENGTH = 2 + 4;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() > BASE_LENGTH) {
            in.markReaderIndex();
            if (in.readShort() != RpcConstants.MAGIC_NUMBER) {
                log.info("invalid magic number, skip it and close ctx");
                ctx.close();
                return;
            }
            int contentLength = in.readInt();
            if (in.readableBytes() < contentLength) {
                in.resetReaderIndex();
                return;
            }
            byte[] content = new byte[contentLength];
            in.readBytes(content);
            RpcProtocol rpcProtocol = new RpcProtocol(content);
            out.add(rpcProtocol);
        }
    }
}
