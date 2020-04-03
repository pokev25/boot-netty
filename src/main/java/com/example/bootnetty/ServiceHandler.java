package com.example.bootnetty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;

@Slf4j
@Component
@ChannelHandler.Sharable
public class ServiceHandler extends ChannelInboundHandlerAdapter {

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
    public void channelActive(ChannelHandlerContext ctx){
        log.info("channel activated {}", ctx.channel());
        serverManager.getChannels().add(ctx.channel());
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx){
        log.info("channel deactivated {}", ctx.channel());
        serverManager.getChannels().remove(ctx.channel());
        ctx.fireChannelInactive();
    }

    /**
     * Channel read.
     *
     * @param ctx the ctx
     * @param msg the msg
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        ByteBuf byteBuf = (ByteBuf) msg;
        log.debug("message : {} ",byteBuf.toString(Charset.defaultCharset()));
        ctx.writeAndFlush(msg);
    }
}
