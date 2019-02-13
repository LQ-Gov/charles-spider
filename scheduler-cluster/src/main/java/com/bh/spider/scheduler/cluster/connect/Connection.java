package com.bh.spider.scheduler.cluster.connect;

import com.bh.spider.scheduler.BasicScheduler;
import com.bh.spider.scheduler.Scheduler;
import com.bh.spider.scheduler.event.Command;
import com.bh.spider.transfer.CommandCode;
import com.bh.spider.transfer.Json;
import com.bh.transport.Transport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public class Connection implements Closeable {

    private final static Logger logger = LoggerFactory.getLogger(Connection.class);

    private URI uri;
    private Channel channel;
    private Communicator communicator;

    private Scheduler scheduler;


    private Cache<Long,Boolean> commandCache;


    public Connection(URI uri, Communicator communicator, Scheduler scheduler) {
        this.uri = uri;
        this.communicator = communicator;
        this.scheduler = scheduler;

        commandCache = Caffeine.newBuilder()
                .maximumSize(Long.MAX_VALUE)
                .expireAfterWrite(1, TimeUnit.MINUTES).build();
    }



    public void open() throws InterruptedException {
        Connection me = this;
        Bootstrap bootstrap = new Bootstrap()
                .group(new EpollEventLoopGroup(3))
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new IdleStateHandler(0,10,0));
                        ch.pipeline().addLast(new CommandInBoundHandler(me,scheduler));
                    }
                });

        ChannelFuture future = bootstrap.connect(new InetSocketAddress(uri.getHost(), uri.getPort())).sync();
        channel = future.channel();
    }


    @Override
    public void close() throws IOException {

    }

    public void write(long id,Command command, boolean cache) throws JsonProcessingException {
        short cmdCode = (short) CommandCode.valueOf(command.key()).ordinal();
        byte[] data = Json.get().writeValueAsBytes(command.params());

        data = Transport.request(id, cmdCode, data);

        ByteBuf buf = channel.alloc().buffer(data.length);//id,flag,len,data
        buf.writeBytes(data);

        channel.write(buf);

        if (cache)
            commandCache.put(id, true);
    }



    public void heartBeat(){

    }


}