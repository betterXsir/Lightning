package jcrazy.communication;

import com.sun.org.apache.regexp.internal.RE;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import jcrazy.entity.Request;
import jcrazy.entity.Response;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;

public class MessageServerHandler extends ChannelInboundHandlerAdapter{
    private int count = 0;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Request request = (Request) msg;
        Response response = new Response();
        response.setId(request.getId());
        response.setResult("success");
        count ++;
        ctx.writeAndFlush(response);
        System.out.printf("has sended %d messages.\n", count);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("通讯异常 : " + cause);
        ctx.close();
    }
}
