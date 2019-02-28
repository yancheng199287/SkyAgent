package com.oneinlet.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialRequest
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod
import org.slf4j.LoggerFactory

class Socks5InitialRequestHandler : SimpleChannelInboundHandler<DefaultSocks5InitialRequest>() {

    private val logger = LoggerFactory.getLogger("Socks5InitialRequestHandler")


    override fun channelRead0(ctx: ChannelHandlerContext?, msg: DefaultSocks5InitialRequest) {
        // 设置是否需要密码，这里设置为无
        if (ctx!!.channel().isActive) {
            val initialResponse = DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH)
            ctx.writeAndFlush(initialResponse)
        }
    }

}