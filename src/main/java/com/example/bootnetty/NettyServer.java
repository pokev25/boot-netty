package com.example.bootnetty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Slf4j
@Component
public class NettyServer implements ApplicationRunner {

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

    public NettyServer(ServiceHandler serviceHandler){
        this.serviceHandler = serviceHandler;
    }

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture channelFuture;
    /**
     * Start.
     */
    public void start() {
        /*
         * 클라이언트 연결을 수락하는 부모 스레드 그룹
         */
        bossGroup = new NioEventLoopGroup(bossCount);
        /*
         * 연결된 클라이언트의 소켓으로 부터 데이터 입출력 및 이벤트를 담당하는 자식 스레드
         */
        workerGroup = new NioEventLoopGroup(workerCount);

        try {
            sb = new ServerBootstrap();
            sb.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)                              //서버 소켓 입출력 모드를 NIO로 설정
                    //.option(ChannelOption.TCP_NODELAY,true)
                    //.option(ChannelOption.SO_REUSEADDR,true)
                    .option(ChannelOption.SO_BACKLOG, backlog)
                    .handler(new LoggingHandler(LogLevel.INFO))                         //서버 소켓 채널 핸들러 등록
                    .childHandler(new ChannelInitializer<SocketChannel>() {             //송수신 되는 데이터 가공 핸들러
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            addPipeline(ch);
                        }
                    });

            channelFuture = sb.bind(tcpPort).sync();
            channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            log.error(e.getMessage(),e);
            shutdown();
        }
    }

    private void addPipeline(SocketChannel sc){
        ChannelPipeline cp = sc.pipeline();
        cp.addLast(new LoggingHandler(LogLevel.INFO));
        cp.addLast(serviceHandler);
    }

    private void shutdown(){
        if(channelFuture != null){
            channelFuture.channel().close().syncUninterruptibly();
            workerGroup.shutdownGracefully().syncUninterruptibly();
            bossGroup.shutdownGracefully().syncUninterruptibly();
        }
    }

    @PreDestroy
    public void stop(){
        shutdown();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        start();
    }
}
