package com.example.demo.nettyclient;

/**
 * Created by fangqing on 11/11/17.
 */
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class NettyClient {

    public static Channel ch;
    private String host;
    private int port;

    public NettyClient(String host, int port) {
        super();
        this.host = host;
        this.port = port;
    }

    public void start() {
        try {
            new Thread(new NettyRunnable()).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class NettyRunnable implements Runnable {

        public void run() {

            while (true) {
                System.out.println("启动线程，连接" + host + ":" + port);
                try {
                    NettyClient.connect(host, port);
                } catch (Exception e) {
                    try {
                        Thread.sleep(6000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    public static synchronized void connect(String host, int port) throws Exception {
        if (host == null || port < 0) {
            throw new NullPointerException("socket unconnected");
        }

        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000).handler(new ClientChildChannelHandler());

            ch = b.connect(host, port).sync().channel();
            ch.closeFuture().sync();
            System.out.println("................断开连接................");

        } finally {
            group.shutdownGracefully();
        }
    }

    private static class ClientChildChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
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
            ch.pipeline().addLast(new HeartBeatReqHandler());

            ch.pipeline().addLast(new Pipe1ClientHandler());
            ch.pipeline().addLast(new Pipe2ClientHandler());
        }

    }

    public static void main(String[] args) {
        new NettyClient("127.0.0.1", 55025).start();
    }

}