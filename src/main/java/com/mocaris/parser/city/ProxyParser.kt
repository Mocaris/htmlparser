package com.mocaris.parser.city

import com.mocaris.model.*
import com.mocaris.parser.BaseParser
import org.jsoup.*
import java.net.*


/**
 *爬取 可用的 代理ip
 * @Author mocaris
 * @Date 2019/6/2-16:57
 */
class ProxyParser : BaseParser<ProxyIp>() {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Thread(ProxyParser()).start()
        }
    }

    private val PROXY_URL = "http://www.qydaili.com/free/?action=china&page="

    //获取3页的免费代理Ip
    override fun run() {
        try {
            val ipList = mutableListOf<ProxyIp>()
            for (page in 1 until 6) {
                println("---------------- 开始获取代理ip:第 $page 页---------------- ")
                val proxyDoc = Jsoup.parse(URL(getProxyPageUrl(1)), 20000)
                val tbodies = proxyDoc.body().getElementsByTag("tbody")
                if (tbodies.isNotEmpty()) {
                    val tdBody = tbodies[0]
                    val ipRows = tdBody.getElementsByTag("tr")
                    //解析ip地址
                    for (ipRow in ipRows) {
                        val data_title = ipRow.getElementsByAttribute("data-title")
                        val ip = data_title[0].html()
                        val port = data_title[1].html()
                        ipList.add(ProxyIp(ip, port))
                    }
                }
                println("---------------- 第 $page 页代理ip获取完成---------------- ")
                Thread.sleep(DELAY_TIME)
            }
            println("----------------代理ip获取完成---------------- ")
            listener?.onSuccess(ipList)
        } catch (e: Exception) {
            e.printStackTrace()
            listener?.onFailed(e.message ?: "")
        }
    }

    //代理ip 页面
    private fun getProxyPageUrl(page: Int): String {
        return "$PROXY_URL$page"
    }
}