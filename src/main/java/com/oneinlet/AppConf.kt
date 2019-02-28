package com.oneinlet

import java.io.*
import java.util.*

/**
 *  Created by WangZiHe on 2017/8/29.
 * QQ/WeChat:648830605
 * QQ-Group:368512253
 * Blog:www.520code.net
 */

object AppConf {

    private val separator = System.getProperty("file.separator")
    private val map = HashMap<String, String>(5)

    val accessKey = map["accessKey"]

    fun getStringValue(): HashMap<String, String> {
        if (map.size <= 0) {
            val path = System.getProperty("user.dir") + separator + "app.properties"
            if (File(path).exists()) {
                getCurrentConf()
            } else {
                getClassPathConf()
            }
        }
        return map
    }


    private fun getClassPathConf(): HashMap<String, String> {
        val resourceBundle = ResourceBundle.getBundle("app")
        resourceBundle.keySet().forEach {
            run {
                val value = resourceBundle.getString(it)
                map.put(it, value)
                // println("current config params：key:$it , value:$value")
            }
        }
        return map
    }


    private fun getCurrentConf() {
        val path = System.getProperty("user.dir") + separator + "app.properties"
        var resourceBundle: ResourceBundle? = null
        try {
            BufferedInputStream(FileInputStream(path)).use { inputStream -> resourceBundle = PropertyResourceBundle(inputStream) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val iterator = resourceBundle!!.keySet().iterator()
        while (iterator.hasNext()) {
            val it = iterator.next()
            val value = resourceBundle!!.getString(it)
            map[it] = value
            println("current config params：key:$it , value:$value")
        }
    }

}