package org.idea.irpc.framework.core.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;

/**
 * @author cyang
 */
@Slf4j
public class CommonUtils {
    public static boolean isEmptyList(List<?> objects) {
        return objects == null || objects.isEmpty();
    }

    public static boolean isNotEmptyList(List<?> objects) {
        return !isEmptyList(objects);
    }
    public static String getIpAddress() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                }
                String displayName = netInterface.getDisplayName();
                // 排除常见的虚拟网卡
                if (displayName.contains("docker") || displayName.contains("br-") || displayName.contains("vmnet") || displayName.contains("vbox") || displayName.contains("tailscale")) {
                    continue;
                }
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = addresses.nextElement();
                    if (ip instanceof Inet4Address) {
                        log.debug("网卡名称: {}, ip: {}", netInterface.getDisplayName(), ip.getHostAddress());
                        return ip.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            log.error("IP地址获取失败",  e);
        }
        return "";
    }
}
