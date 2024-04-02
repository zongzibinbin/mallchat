package com.abin.mallchat.common.websocket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

/**
 * WebSocket协议类型的模拟客户端连接器类
 *
 * @author duyanjun
 * @since 2022/10/13 杜燕军 新建
 */
@Slf4j
public class WebSocketConnector {
    // 服务器ip
    protected String serverIp;
    // 服务器通信端口
    protected int serverSocketPort;
    // 事件循环线程池
    protected EventLoopGroup group;
    // 网络通道
    private Channel channel;

    /**
     * WebSocket协议类型的模拟客户端连接器构造方法
     *
     * @param serverIp
     * @param serverSocketPort
     * @param group
     */
    public WebSocketConnector(String serverIp, int serverSocketPort, EventLoopGroup group) {
        this.serverIp = serverIp;
        this.serverSocketPort = serverSocketPort;
        this.group = group;
    }

    public void doConnect() {
        try {
            String URL = "ws://" + this.serverIp + ":" + this.serverSocketPort + "/";
            URI uri = new URI(URL);
            final WebSocketIoHandler handler =
                    new WebSocketIoHandler(
                            WebSocketClientHandshakerFactory.newHandshaker(
                                    uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()));
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    //.option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            //空闲事件
                            pipeline.addLast(new IdleStateHandler(0, 10, 0));
                            // 添加一个http的编解码器
                            pipeline.addLast(new HttpClientCodec());
                            // 添加一个用于支持大数据流的支持
                            pipeline.addLast(new ChunkedWriteHandler());
                            // 添加一个聚合器，这个聚合器主要是将HttpMessage聚合成FullHttpRequest/Response
                            pipeline.addLast(new HttpObjectAggregator(1024 * 64));
                            pipeline.addLast(handler);
                        }
                    });
            try {
                synchronized (bootstrap) {
                    final ChannelFuture future = bootstrap.connect(this.serverIp, this.serverSocketPort).sync();
                    this.channel = future.channel();
                }
            } catch (InterruptedException e) {
                log.error("连接服务失败.......................uri:" + uri.toString(), e);
            } catch (Exception e) {
                log.error("连接服务失败.......................uri:" + uri.toString(), e);
            }
        } catch (Exception e) {
            log.error("连接服务失败.......................", e);
        } finally {
        }

    }

    public void disConnect() {
        this.channel.close();
    }
}
