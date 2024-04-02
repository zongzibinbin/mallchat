package com.abin.mallchat.common.websocket;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public class Simulator {
    public static void start() {
        String serverIp = "101.33.251.36";
        int serverPort = 8090;
        EventLoopGroup group = new NioEventLoopGroup();
        for (int i = 0; i < 10000; i++) {
            WebSocketConnector client = new WebSocketConnector(serverIp, serverPort, group);
            client.doConnect();
        }
    }

    public static void main(String[] args) {
        start();
    }
}
