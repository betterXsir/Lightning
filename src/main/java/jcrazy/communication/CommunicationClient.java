package jcrazy.communication;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import jcrazy.config.ProtocolConfig;
import jcrazy.entity.Request;
import jcrazy.entity.Response;
import jcrazy.protocol.protobuf.ProtobufNettyDecoder;
import jcrazy.protocol.protobuf.ProtobufNettyEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class CommunicationClient {
    @Autowired
    private ProtocolConfig protocolConfig;

    private MessageClientHandler messageClientHandler;

    public void startUpClient() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            //socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(62235, 0, 2, 0, 2));
                            //socketChannel.pipeline().addLast(new LengthFieldPrepender(2));
                            socketChannel.pipeline().addLast(new ProtobufNettyDecoder(Response.class));
                            socketChannel.pipeline().addLast(new ProtobufNettyEncoder());
                            socketChannel.pipeline().addLast(new MessageClientHandler());
                        }
                    });
            //.sync()阻塞直到连接成功
            ChannelFuture future = bootstrap.connect("127.0.0.1", protocolConfig.getPort());

            MessageClientHandler handler = null;
            future.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if(channelFuture.isSuccess()) {
                        MessageClientHandler messageHandler = channelFuture.channel()
                                .pipeline().get(MessageClientHandler.class);
                        messageClientHandler = messageHandler;
                    }
                }
            });

            //wait until the connection is closed
            future.channel().closeFuture().sync();
        } catch (InterruptedException var) {
            System.out.println("中断异常");
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public MessageClientHandler getMessageClientHandler() {
        return messageClientHandler;
    }

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("lightning-service-consumer.xml");
        CommunicationClient communicationClient = (CommunicationClient) context.getBean("communicationClient");
        for(int i = 0; i < 1; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        communicationClient.startUpClient();
                        while(communicationClient.getMessageClientHandler() == null) {
                            ;
                        }
                        communicationClient.getMessageClientHandler().sendMessage(new Request());
                    } catch (Exception var) {
                        var.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
