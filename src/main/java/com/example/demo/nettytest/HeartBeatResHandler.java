package com.example.demo.nettytest;

/**
 * Created by fangqing on 11/11/17.
 */


import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class HeartBeatResHandler extends ChannelHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            String message = (String) msg;

            if (!message.contains("heart")) {
                ctx.fireChannelRead(msg);
                return;
            }

            System.out.println("HeartBeatResHandler:" + message);
            ctx.writeAndFlush(Unpooled.copiedBuffer("heartBeat$_".getBytes()));

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
