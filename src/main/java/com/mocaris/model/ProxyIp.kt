package com.mocaris.model


/**
 *  代理Ip
 * @Author mocaris
 * @Date 2019/6/2-16:58
 */
data class ProxyIp(val ipAddress: String, val port: String) {

    override fun toString(): String {
        return "ProxyIp(ipAddress='$ipAddress', port='$port')\n"
    }
}