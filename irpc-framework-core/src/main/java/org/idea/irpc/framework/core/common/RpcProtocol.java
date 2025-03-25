package org.idea.irpc.framework.core.common;

import lombok.Data;
import org.idea.irpc.framework.core.common.constants.RpcConstants;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author cyang
 */
@Data
public class RpcProtocol implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    short magicNumber;
    int contentLength;
    byte[] content;

    public RpcProtocol(byte[] content) {
        this.magicNumber = RpcConstants.MAGIC_NUMBER;
        this.contentLength = content.length;
        this.content = content;
    }
}
