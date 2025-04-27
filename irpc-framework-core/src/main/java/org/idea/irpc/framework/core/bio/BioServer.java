package org.idea.irpc.framework.core.bio;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BioServer {
    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(10);
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(8009));
        try {
            while (true) {
                Socket socket = serverSocket.accept();
                EXECUTOR.submit(() -> {
                    while (true) {
                        InputStream inputStream = socket.getInputStream();
                        byte[] buffer = new byte[1024];
                        int len = inputStream.read(buffer);
                        String message = new String(buffer, 0, len);
                        System.out.println(message);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
