package com.bh.spider.consistent.raft.transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * @author liuqi19
 * @version : ClientConnection, 2019-04-16 16:10 liuqi19
 */
public class ClientConnection extends Connection {
    private final static Logger logger = LoggerFactory.getLogger(ClientConnection.class);

    private final static EventLoopGroup loop = new NioEventLoopGroup();

    private InetSocketAddress address;

    public ClientConnection(String ip, int port) {
        this.address = new InetSocketAddress(ip, port);


    }

    public ChannelFuture connect(ChannelHandler... channelHandlers) {
        logger.info("try to connect remote server:{}:{}", address.getHostName(), address.getPort());
        Bootstrap bootstrap = new Bootstrap()
                .group(loop)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(channelHandlers);
                    }
                })
                .remoteAddress(address);


        ChannelFuture future = bootstrap.connect();
        future.addListener((ChannelFutureListener) f -> {
            if (!f.isSuccess()) {
                EventLoop loop = f.channel().eventLoop();
                loop.schedule(() -> connect(channelHandlers), 1L, TimeUnit.SECONDS);
            }
        });
//        this.setChannel(future.channel());
        return future;
    }
}
