package com.oneinlet.server

import com.oneinlet.handler.Socks5CommandRequestHandler
import com.oneinlet.handler.Socks5InitialRequestHandler
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder
import io.netty.handler.codec.socksx.v5.Socks5PasswordAuthRequestDecoder
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder
import io.netty.handler.timeout.IdleStateHandler
import org.slf4j.LoggerFactory


class SkyAgentServer(private val serverPort: Int) {

    private val logger = LoggerFactory.getLogger("SkyAgentServer")

    fun start() {

        val bossGroup = NioEventLoopGroup()
        val workerGroup = NioEventLoopGroup()
        val serverBootstrap = ServerBootstrap()
        try {
            serverBootstrap.group(bossGroup, workerGroup)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                    .channel(NioServerSocketChannel::class.java)
                    .childHandler(
                            object : ChannelInitializer<SocketChannel>() {
                                override fun initChannel(ch: SocketChannel?) {
                                    ch!!.pipeline()
                                    ch.pipeline()
                                            .addLast(IdleStateHandler(5, 30, 0))
                                            .addLast(Socks5ServerEncoder.DEFAULT) // 服务端的编码操作
                                            .addLast(Socks5InitialRequestDecoder()) // 请求解码操作
                                            .addLast(Socks5InitialRequestHandler())
                                            //  .addLast(Socks5PasswordAuthRequestDecoder()) //验证密码
                                            .addLast(Socks5CommandRequestDecoder())  //处理连接操作
                                            .addLast(Socks5CommandRequestHandler())
                                }
                            })
            val f: ChannelFuture = serverBootstrap.bind(serverPort).sync()
            val address = f.channel().localAddress()
            logger.info("App Server has been started，address:$address ,  please enjoy it！")
            f.channel().closeFuture().sync()
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
            logger.warn("App server has been shutdown， EventLoopGroup  has been released all resources！")
        }

    }

}