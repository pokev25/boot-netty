package com.example.bootnetty.handler;

import com.example.bootnetty.netty.ServerManager;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

@Slf4j
@Component
@ChannelHandler.Sharable
public class ServiceHandler extends SimpleChannelInboundHandler<String> {

    /**
     * The Channels.
     */
    private final ServerManager serverManager;

    public ServiceHandler(ServerManager serverManager){
        this.serverManager = serverManager;
    }

    @PostConstruct
    public void init() {
        log.info("BaseHandler init");
    }

    /**
     * Channel active.
     *
     * @param ctx the ctx
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws UnknownHostException {
        log.info("channel activated {}", ctx.channel());
        serverManager.getChannels().add(ctx.channel());
        ctx.fireChannelActive();
        // Send greeting for a new connection.
        ctx.write("Welcome to " + InetAddress.getLocalHost().getHostName() + "!\r\n");
        ctx.write("It is " + new Date() + " now.\r\n");
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx){
        log.info("channel deactivated {}", ctx.channel());
        serverManager.getChannels().remove(ctx.channel());
        ctx.fireChannelInactive();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {
        // Generate and write a response.
        String response;
        boolean close = false;
        if (request.isEmpty()) {
            response = "Please type something.\r\n";
        } else if ("bye".equals(request.toLowerCase())) {
            response = "Have a good day!\r\n";
            close = true;
        } else {
            response = "Did you say '" + request + "'?\r\n";
        }

        // We do not need to write a ChannelBuffer here.
        // We know the encoder inserted at TelnetPipelineFactory will do the conversion.
        ChannelFuture future = ctx.write(response);

        // Close the connection after sending 'Have a good day!'
        // if the client has sent 'bye'.
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
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
