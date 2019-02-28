package com.oneinlet.handler

import io.netty.channel.Channel
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelHandlerContext
import io.netty.util.concurrent.Promise


class DirectClientHandler(val promise: Promise<Channel>) : ChannelInboundHandlerAdapter() {


    override fun channelActive(ctx: ChannelHandlerContext) {
        ctx.pipeline().remove(this)
        //触发成功事件
        promise.setSuccess(ctx.channel())
    }


    override fun exceptionCaught(ctx: ChannelHandlerContext, throwable: Throwable) {
        //触发失败事件
        promise.setFailure(throwable)
    }

}