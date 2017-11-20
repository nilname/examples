package com.example.demo.nettyclient;

/**
 * Created by fangqing on 11/11/17.
 */
import java.util.concurrent.TimeUnit;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class HeartBeatReqHandler extends ChannelHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            String message = (String) msg;

            if (!message.contains("heart")) {
                ctx.fireChannelRead(msg);
                return;
            }

            System.out.println("HeartBeatReqHandler: " + message);
            ctx.writeAndFlush(Unpooled.copiedBuffer("pipe1_client $_".getBytes()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.executor().scheduleAtFixedRate(new HeartBeatTask(ctx), 0, 90000, TimeUnit.MILLISECONDS);
    }

    private class HeartBeatTask implements Runnable {
        private final ChannelHandlerContext ctx;

        public HeartBeatTask(final ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        public void run() {
            ctx.writeAndFlush(Unpooled.copiedBuffer("heartBeat$_".getBytes()));
        }

    }

}
