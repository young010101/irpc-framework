
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import static org.idea.irpc.framework.core.common.utils.CommonUtils.getIpAddress;

@Slf4j
public class NetInterfaceTest {

    public static void main(String[] args) {
        try {
            Enumeration<NetworkInterface> allIs = NetworkInterface.getNetworkInterfaces();
            while (allIs.hasMoreElements()) {
                NetworkInterface networkInterface = allIs.nextElement();
                if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()) continue;

                System.out.println(networkInterface.getDisplayName());
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (inetAddress instanceof Inet4Address)
                        System.out.print(inetAddress.getHostAddress() + " ");
                }
                System.out.println();
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }



    @Test
    void test() {
        System.out.println(getIpAddress());
    }
}
