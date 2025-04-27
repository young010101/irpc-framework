package org.idea.irpc.framework.core.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * RPC协议编码器
 * 负责将RpcProtocol对象编码为字节流
 * @author cyang
 */
public class RpcEncoder extends MessageToByteEncoder<RpcProtocol> {
    
    /**
     * 将RpcProtocol对象编码为字节流
     * 编码格式:
     * <ul>
     * <li>2字节魔数(magic number)</li>
     * <li>4字节内容长度(content length)</li>
     * <li>实际内容(content)</li>
     * </ul>
     * @see RpcProtocol
     *
     * @param ctx ChannelHandlerContext
     * @param msg 待编码的RpcProtocol对象
     * @param out 输出缓冲区
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcProtocol msg, ByteBuf out) {
        out.writeShort(msg.getMagicNumber());
        out.writeInt(msg.getContentLength());
        out.writeBytes(msg.getContent());
    }
}
