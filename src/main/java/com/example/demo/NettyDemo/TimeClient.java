package com.example.demo.NettyDemo;

/**
 * Created by fangqing on 11/25/17.
 */

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.charset.Charset;

public class TimeClient {
    public static void main(String[] args) throws Exception {
        String host = "127.0.0.1";
        int port = 8080;
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap(); // (1)
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new TimeClientHandler());
                }
            });

            // Start the client.
            ChannelFuture f = b.connect(host, port).sync(); // (5)

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}


class TimeClientHandler extends ChannelHandlerAdapter {
    private int index = 0;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            //虽然这里可以给服务端发消息但是我想在其他地方需要发消息的时候再发而不是收到服务端发来消息才发
            ByteBuf buf = (ByteBuf) msg;
            System.out.print(buf.toString(Charset.forName("UTF-8")));
        } finally {

        }
        if (index < 100) {
            final ByteBuf buff = ctx.alloc().buffer(); // (2)
            buff.writeBytes("客户端\n".getBytes(Charset.forName("UTF-8")));
            ctx.writeAndFlush(buff);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            index++;
        } else {
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }
}

