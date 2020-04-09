package com.example.bootnetty.handler;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.ipfilter.AbstractRemoteAddressFilter;
import io.netty.handler.ipfilter.IpFilterRule;
import io.netty.handler.ipfilter.IpFilterRuleType;
import io.netty.handler.ipfilter.IpSubnetFilterRule;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;

@Slf4j
@ChannelHandler.Sharable
public class IpFilterHandler extends AbstractRemoteAddressFilter<InetSocketAddress> {

    private final Set<IpFilterRule> rules = Sets.newHashSet();

    /**
     * ip filter
     * @param denyIPs 거부 IP 목록 , 로 구분
     */
    public IpFilterHandler(String denyIPs) {
        List<String> ips = Splitter.on(',')
                .trimResults()
                .omitEmptyStrings()
                .splitToList(denyIPs);

        for (String denyIP : ips) {
            if (denyIP.contains("/")) {
                String[] split = denyIP.split("/");
                String ip = split[0];
                int cidr = Integer.parseInt(split[1]);
                this.rules.add(new IpSubnetFilterRule(ip, cidr, IpFilterRuleType.REJECT));
            } else {
                this.rules.add(new IpSubnetFilterRule(denyIP, 32, IpFilterRuleType.REJECT));
            }
        }
    }

    @Override
    public boolean accept(ChannelHandlerContext ctx, InetSocketAddress remoteAddress) {
        for (IpFilterRule rule : rules) {
            if (rule == null) {
                break;
            }

            if (rule.matches(remoteAddress)) {
                return rule.ruleType() == IpFilterRuleType.ACCEPT;
            }
        }
        return true;
    }
}
