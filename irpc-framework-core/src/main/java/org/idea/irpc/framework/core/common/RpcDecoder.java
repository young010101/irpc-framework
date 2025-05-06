package org.idea.irpc.framework.core.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.idea.irpc.framework.core.common.constants.RpcConstants;

import java.util.List;

/**
 * RPC协议解码器
 * 负责将字节流解码为RpcProtocol对象
 * @author cyang
 */
@Slf4j
public class RpcDecoder extends ByteToMessageDecoder {
    private static final int BASE_LENGTH = 2 + 4;
    /// 最大帧长度限制
    private static final int MAX_FRAME_LENGTH = 1000;

    /**
     * 将字节流解码为RpcProtocol对象
     * 解码格式:
     * <ul>
     * <li>2字节魔数(magic number)</li>
     * <li>4字节内容长度(content length)</li>
     * <li>实际内容(content)</li>
     * </ul>
     * 
     * 如果魔数不匹配或内容长度不足,将重置读取位置并等待更多数据. 
     * 如果数据包超过最大长度限制,将跳过该数据包
     *
     * @param ctx ChannelHandlerContext
     * @param in 输入缓冲区
     * @param out 解码后的对象列表
     * @see RpcProtocol
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 防止收到过大的数据包
        if (in.readableBytes() > MAX_FRAME_LENGTH) {
            in.skipBytes(in.readableBytes());
            return;
        }

        if (in.readableBytes() >= BASE_LENGTH) {
//            in.readerIndex();
            in.markReaderIndex();
            
            // 验证魔数
            if (in.readShort() != RpcConstants.MAGIC_NUMBER) {
                log.error("invalid magic number, skip it and close ctx");
                ctx.close();
                return;
            }
            
            // 读取内容长度
            int contentLength = in.readInt();
            // 如果收到的包不完整, 也就是发生了半包
            if (in.readableBytes() < contentLength) {
                // 重置标记位置, 等接收到完整的包再读取
                in.resetReaderIndex();
                return;
            }
            
            // 读取实际内容
            byte[] content = new byte[contentLength];
            in.readBytes(content);
            RpcProtocol rpcProtocol = new RpcProtocol(content);
            out.add(rpcProtocol);
        }
    }
}
