package com.example.bootnetty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class NettyServer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * The Tcp port.
     */
    @Value("${netty.port}")
    private int tcpPort;

    /**
     * The Boss count.
     */
    @Value("${netty.threads.acceptor}")
    private int bossCount;

    /**
     * The Worker count.
     */
    @Value("${netty.threads.worker}")
    private int workerCount;

    @Value("${netty.backlog}")
    private int backlog;

    /**
     * The constant SERVICE_HANDLER.
     */
    private final ServiceHandler serviceHandler;

    private ServerBootstrap sb;

    @Autowired
    public NettyServer(ServiceHandler serviceHandler){
        this.serviceHandler = serviceHandler;
    }

    /**
     * Start.
     */
    @PostConstruct
    public void start() {
        /*
         * 클라이언트 연결을 수락하는 부모 스레드 그룹
         */
        EventLoopGroup bossGroup = new NioEventLoopGroup(bossCount);
        /*
         * 연결된 클라이언트ㄹ의 소켓으로 부터 데이터 입출력 및 이벤트를 담당하는 자식 스레드
         */
        EventLoopGroup workerGroup = new NioEventLoopGroup(workerCount);

        sb = new ServerBootstrap();
        sb.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)                              //서버 소켓 입출력 모드를 NIO로 설정
                .option(ChannelOption.SO_BACKLOG, backlog)
                .handler(new LoggingHandler(LogLevel.INFO))                         //서버 소켓 채널 핸들러 등록
                .childHandler(new ChannelInitializer<SocketChannel>() {             //송수신 되는 데이터 가공 핸들러
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        addPipeline(ch);
                    }
                });

        try {
            sb.bind(tcpPort);
        } catch (Exception e) {
            logger.info(e.getMessage(),e);
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private void addPipeline(SocketChannel sc){
        ChannelPipeline cp = sc.pipeline();
        cp.addLast(new LoggingHandler(LogLevel.INFO));
        cp.addLast(serviceHandler);
    }
}
