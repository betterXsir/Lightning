package jcrazy.communication;

import com.sun.org.apache.regexp.internal.RE;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import jcrazy.entity.Request;
import jcrazy.entity.Response;

import java.io.IOException;
import java.util.UUID;

public class MessageClientHandler extends ChannelInboundHandlerAdapter{
    private Channel channel;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //final ChannelFuture future = ctx.writeAndFlush(request);
        channel = ctx.channel();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Response response = (Response)msg;
        System.out.printf("response %s : %s\n", response.getId(), response.getResult());
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("通讯异常: " + cause);
        ctx.close();
    }

    public void sendMessage(Request request) {
        ChannelFuture future = channel.writeAndFlush(request);
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                System.out.printf("send message %s\n", request.getId());
            }
        });
    }
}
