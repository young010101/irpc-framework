package org.idea.irpc.framework.core.common;

import lombok.Data;
import org.idea.irpc.framework.core.common.constants.RpcConstants;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;

/**
 * RPC 协议类，定义了网络传输的数据格式
 * <p>
 * 协议格式：
 * <pre>
 * +----------------+------------------+----------------+
 * | magicNumber(2) | contentLength(4) | content(n)     |
 * +----------------+------------------+----------------+
 * </pre>
 * </p>
 *
 * @author cyang
 * @since 2024-03
 */
@Data
public class RpcProtocol implements Serializable {
    /**
     * 序列化版本ID
     * <p>
     * 格式：YYYYMMDDVV.
     * 示例：2024030101 表示2024年3月1日的第1个版本
     * </p>
     */
    @Serial
    private static final long serialVersionUID = 2025042701L;

    /**
     * 魔数，用于标识 RPC 协议
     */
    private final short magicNumber;

    /**
     * 内容长度，表示 content 字段的字节数
     */
    private final int contentLength;

    /**
     * 协议内容，为 RpcInvocation 类的序列化字节数组
     * @see RpcInvocation
     */
    private final byte[] content;

    /**
     * 构造方法
     *
     * @param content 协议内容，不能为 null
     * @throws IllegalArgumentException 如果 content 为 null
     */
    public RpcProtocol(byte[] content) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }
        this.magicNumber = RpcConstants.MAGIC_NUMBER;
        this.contentLength = content.length;
        // 防御性拷贝
        this.content = Arrays.copyOf(content, content.length);
    }
}
