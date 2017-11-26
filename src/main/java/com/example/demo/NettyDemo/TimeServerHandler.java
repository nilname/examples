package com.example.demo.NettyDemo;

/**
 * Created by fangqing on 11/25/17.
 */
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.Charset;

public class TimeServerHandler extends ChannelHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)

        //接收
        ByteBuf in = (ByteBuf) msg;
        try {

            System.out.print(in.toString(io.netty.util.CharsetUtil.UTF_8));

//            while (in.isReadable()) { // (1)
//                System.out.print((char) in.readByte());
////                System.out.flush();
//            }
        } finally {
            ReferenceCountUtil.release(msg); // (2)
        }
        //发送
        ByteBuf ins=ctx.alloc().buffer();
        ins.writeBytes("这是服务端".getBytes(Charset.forName("UTF-8")));
        ctx.writeAndFlush(ins); // (1)
//        ctx.flush(); // (2)
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) { // (1)
        final ByteBuf buff = ctx.alloc().buffer(64); // (2)
        buff.writeBytes("欢迎光临".getBytes(Charset.forName("UTF-8")));
        ctx.writeAndFlush(buff);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
