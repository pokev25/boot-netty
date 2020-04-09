package com.example.bootnetty.netty;

import com.example.bootnetty.handler.IpFilterHandler;
import com.example.bootnetty.handler.ServiceHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import org.springframework.stereotype.Component;

@Component
public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    private final ServiceHandler serviceHandler;

    private static final StringDecoder DECODER = new StringDecoder(CharsetUtil.UTF_8);
    private static final StringEncoder ENCODER = new StringEncoder(CharsetUtil.UTF_8);

    public ServerInitializer(ServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        String denyIps = "";
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
        pipeline.addLast("ip filter",new IpFilterHandler(denyIps));
        pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        pipeline.addLast("stringDecoder",DECODER);
        pipeline.addLast("stringEncoder",ENCODER);
        pipeline.addLast(serviceHandler);
    }
}
