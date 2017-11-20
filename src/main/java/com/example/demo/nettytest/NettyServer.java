package com.example.demo.nettytest;

/**
 * Created by fangqing on 11/11/17.
 */
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class NettyServer {

    public void bind(int port) throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ServerChildChannelHandler());

            ChannelFuture f = b.bind(port).sync();
            System.out.println("NettyServer："+ port);

            f.channel().closeFuture().sync();

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private class ServerChildChannelHandler extends ChannelInitializer<SocketChannel> {

        protected void initChannel(SocketChannel ch) throws Exception {

            // 使用长度依赖的话需要在数据包前声明数据包的长度
            // ch.pipeline().addLast("lengthField",
            // new LengthFieldBasedFrameDecoder(1024, 0, 2, 0, 2));
            // 换行分割
            // ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
            // ch.pipeline().addLast(new StringDecoder());

            // 自定义分割符
            ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer("$_".getBytes())));
            ch.pipeline().addLast(new StringDecoder());

            ch.pipeline().addLast(new ReadTimeoutHandler(300));
            ch.pipeline().addLast(new HeartBeatResHandler());

            ch.pipeline().addLast(new Pipe1ServerHandler());
            ch.pipeline().addLast(new Pipe2ServerHandler());

        }
    }

    public static void main(String[] args) throws Exception {
        new NettyServer().bind(55025);
    }
}
