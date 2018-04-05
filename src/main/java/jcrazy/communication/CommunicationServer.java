package jcrazy.communication;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import jcrazy.config.ProtocolConfig;
import jcrazy.config.ServiceConfig;
import jcrazy.config.ServiceContainer;
import jcrazy.demo.StudentResource;
import jcrazy.entity.Request;
import jcrazy.protocol.protobuf.ProtobufNettyDecoder;
import jcrazy.protocol.protobuf.ProtobufNettyEncoder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class CommunicationServer implements InitializingBean, ApplicationContextAware{
    @Autowired
    private ProtocolConfig protocolConfig;

    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 该类实例化时, 所有属性都设置好后调用,
     * 如果不实现InitializingBean的话，
     * 使用注解 @PostConstruct,销毁的话 @PreDestory
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        ServiceContainer serviceContainer = null;
        try {
            serviceContainer = (ServiceContainer) applicationContext.getBean("serviceContainer");
            ServiceConfig serviceConfig = (ServiceConfig) applicationContext.getBean("jcrazy.demo.StudentResource");
            ((StudentResource)serviceConfig.getRef()).study();
        }catch (NoSuchBeanDefinitionException var) {
            System.out.println("NoSuchBean");
        }
        if(serviceContainer != null && serviceContainer.getServices() != null && serviceContainer.getServices().size() > 0) {
            try {
                startUpServer();
            }catch (Exception e) {
                System.out.println("服务器异常关闭");
            }
        }
    }

    public void startUpServer() throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();



        try{
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    /**
                     * option() is for the NioServerSocketChannel that accepts incoming connections.
                     * childOption() is for the Channels accepted by the parent ServerChannel,
                     * which is NioServerSocketChannel in this case.
                     * 服务端NioServerSocketChannel处理客户端socket连接是需要一定时间的。它有一个队列，
                     * 存放还没有来得及处理的客户端SocketChannel，这个队列的容量就是backlog的含义
                     */
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)  //保持长连接
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // TODO: 2018/3/30 编码器，解码器
                            //socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(62235, 0, 2, 0, 2));
                            //socketChannel.pipeline().addLast(new LengthFieldPrepender(2));
                            socketChannel.pipeline().addLast(new ProtobufNettyDecoder(Request.class));
                            socketChannel.pipeline().addLast(new ProtobufNettyEncoder());
                            socketChannel.pipeline().addLast(new MessageServerHandler());
                        }
                    });

            /**Bind and start to accept incoming connections, 调用sync(),即同步阻塞方法
             * 同步阻塞等待绑定完成,完成之后返回一个ChannelFurture,主要用于异步操作的通知回调
             */
            ChannelFuture future = bootstrap.bind(protocolConfig.getPort()).sync();
            /**
             * 同步阻塞，等待server socket的关闭
             */
            System.out.println("server bind port successfully, wait for socket's closing...");
            future.channel().closeFuture().sync();
        } catch (Exception var) {
            System.out.println("服务异常 : " + var.getMessage());
            throw var;
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("lightning-service-provider.xml");
        CommunicationServer communicationServer = (CommunicationServer) context.getBean("communicationServer");
    }
}
