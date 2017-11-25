package com.example.demo.nettyclient;

/**
 * Created by fangqing on 11/11/17.
 */
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class Pipe2ClientHandler extends ChannelHandlerAdapter {


    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws InterruptedException { // (1)

        while (true) {
            ByteBuf time = ctx.alloc().buffer(4); //为ByteBuf分配四个字节
            time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));
            ctx.writeAndFlush(time); // (3)
//            Thread.sleep(1000);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            int message = (int) msg;

//            if (!message.contains("pipe2")) {
//                ctx.fireChannelRead(msg);
//                return;
//            }

            System.out.println("Pipe2ClientHandler: " + message);
            ctx.writeAndFlush(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
