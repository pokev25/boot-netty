package com.example.bootnetty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;

@Component
@ChannelHandler.Sharable
public class ServiceHandler extends ChannelInboundHandlerAdapter {
    /**
     * The Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * The Channels.
     */
    private final ServerManager serverManager;

    @Autowired
    public ServiceHandler(ServerManager serverManager){
        this.serverManager = serverManager;
    }

    @PostConstruct
    public void init() {
        logger.info("BaseHandler init");
    }

    /**
     * Channel active.
     *
     * @param ctx the ctx
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx){
        logger.info("channel activated {}", ctx.channel());
        serverManager.getChannels().add(ctx.channel());
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx){
        logger.info("channel deactivated {}", ctx.channel());
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
        logger.debug("message : {} ",byteBuf.toString(Charset.defaultCharset()));
        ctx.writeAndFlush(msg);
    }
}
