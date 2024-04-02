package com.abin.mallchat.common.websocket;

import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket协议类型的模拟客户端IO处理器类
 *
 * @author duyanjun
 * @since 2022/10/13 杜燕军 新建
 */
@Slf4j
public class WebSocketIoHandler extends SimpleChannelInboundHandler<Object> {

    private final WebSocketClientHandshaker handShaker;

    private ChannelPromise handshakeFuture;

    public WebSocketIoHandler(WebSocketClientHandshaker handShaker) {
        this.handShaker = handShaker;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handShaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.close();
        try {
            super.channelInactive(ctx);
        } catch (Exception e) {
            log.error("channelInactive 异常.", e);
        }
        log.warn("WebSocket链路与服务器连接已断开.");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        if (!handShaker.isHandshakeComplete()) {
            try {
                handShaker.finishHandshake(ch, (FullHttpResponse) msg);
                handshakeFuture.setSuccess();
                log.info("WebSocket握手成功，可以传输数据了.");
            } catch (WebSocketHandshakeException e) {
                log.warn("WebSocket Client failed to connect");
                handshakeFuture.setFailure(e);
            }
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.status() +
                            ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            String s = textFrame.text();
            log.info("WebSocket Client received message: " + s);
        } else if (frame instanceof PongWebSocketFrame) {
            log.info("WebSocket Client received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            log.info("WebSocket Client received closing");
            ch.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("WebSocket链路由于发生异常,与服务器连接已断开.", cause);
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            // 如果写通道处于空闲状态,就发送心跳命令
            if (IdleState.WRITER_IDLE.equals(event.state())) {
                // 发送心跳数据
                ctx.writeAndFlush(new TextWebSocketFrame("{\"type\":2}"));
                System.out.println("发送心跳数据");
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
