package com.example.demo.nettytest;

/**
 * Created by fangqing on 11/12/17.
 */

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

/**
 * Created with IntelliJ IDEA.
 * User: ASUS
 * Date: 14-5-7
 * Time: 上午10:10
 * To change this template use File | Settings | File Templates.
 */
public class TimeServer {

    public static void main(String[] args) {
        // EventLoop 代替原来的 ChannelFactory
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    new TimeServerHandler(),
                                    new WriteTimeoutHandler(10),
                                    //控制写入超时10秒构造参数10表示如果持续10秒钟都没有数据写了，那么就超时。
                                    new ReadTimeoutHandler(10)
                            );
                        }
                    }).option(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = serverBootstrap.bind(9090).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}


class TimeServerHandler extends ChannelInboundHandlerAdapter {



    //ChannelHandlerContext通道处理上下文
    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws InterruptedException { // (1)

        while (true) {
            ByteBuf time = ctx.alloc().buffer(4); //为ByteBuf分配四个字节
            time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));
            ctx.writeAndFlush(time); // (3)
//            Thread.sleep(2000);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
