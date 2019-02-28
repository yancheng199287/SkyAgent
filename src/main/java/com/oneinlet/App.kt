package com.oneinlet

import com.oneinlet.server.SkyAgentServer

fun main(args: Array<String>) {
    val port = AppConf.getStringValue()["appPort"]!!.toInt()
    val info = "welcome to use SkyAgent，port: $port  if you have question，join my QQ-Group:368512253"
    println(info)
    SkyAgentServer(port).start()
}