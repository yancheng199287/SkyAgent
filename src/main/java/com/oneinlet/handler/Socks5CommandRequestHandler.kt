package com.oneinlet.handler

import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest
import org.slf4j.LoggerFactory
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener


class Socks5CommandRequestHandler : SimpleChannelInboundHandler<DefaultSocks5CommandRequest>() {

    private val logger = LoggerFactory.getLogger("Socks5CommandRequestHandler")


    override fun channelRead0(ctx: ChannelHandlerContext?, msg: DefaultSocks5CommandRequest?) {
        getResource(ctx!!, msg!!)
    }


    private fun getResource(ctx: ChannelHandlerContext, request: DefaultSocks5CommandRequest) {

        // 创建一个promise  在连接远程服务端后的事件触发后调用
        val promise = ctx.executor().newPromise<Channel>()

        // 等待事件触发，等待下面连接远程服务端的通道连接是否成功
        promise.addListener {

            // 如果连接远程服务的通道成功
            if (it.isSuccess) {

                // 连接远程服务的通道,   promise.setSuccess(ctx.channel()) 这里赋值的通道
                val outboundChannel: Channel = it.now as Channel

                // 告诉客户端连接远程通道成功
                val responseFuture = ctx.channel().writeAndFlush(DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, request.dstAddrType()))
                responseFuture.addListener {
                    // 如果通知已经发送给客户端发送成功
                    if (it.isSuccess) {
                        // 把当前handler删除
                        ctx.pipeline().remove(this)
                        // 远程通道再加一个 替补通道，用来将远程通道读取的返回信息写入到客户端
                        outboundChannel.pipeline().addLast(RelayHandler(ctx.channel()))

                        //客户端通道也增加一个替补通道，用来将本地的请求信息写入到远程通道
                        ctx.pipeline().addLast(RelayHandler(outboundChannel))
                    }
                }
            } else {
                // 告诉客户端连接远程通道失败了
                ctx.channel().writeAndFlush(DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, request.dstAddrType()))
            }

        }


        //获取客户端请求的通道
        val inboundChannel = ctx.channel()
        val bootstrap = Bootstrap()
        bootstrap.group(inboundChannel.eventLoop())
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .channel(inboundChannel.javaClass)
                // connect连接成功会调用
                .handler(DirectClientHandler(promise))


        val remoteHost = request.dstAddr()
        val remotePort = request.dstPort()
        val future = bootstrap.connect(remoteHost, remotePort)


        future.addListener {
            if (it.isSuccess) {
                // 如果连接成功  会调用        .handler(DirectClientHandler(promise))，然后promise会调用
            } else {
                // 如果如果连接远程服务通道失败，那么向客户端发送失败的消息
                val clientChannel = ctx.channel()
                if (clientChannel.isActive) {
                    clientChannel.writeAndFlush(DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, request.dstAddrType()))
                    // 并关闭通道
                    ctx.channel().writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE)
                }
            }
        }

    }


}