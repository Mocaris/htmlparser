package com.mocaris.parser.city

import com.mocaris.ChineseCityParser
import com.mocaris.model.*
import com.mocaris.parser.BaseParser
import org.jsoup.*


/**
 *一级
 *省 直辖市
 * @Author mocaris
 * @Date 2019/6/1-22:17
 */
class ProvinceParser(var proxyIp: ProxyIp) : BaseParser<CityModel>() {

    private val PROVINCE_URL = "${ChineseCityParser.BASE_URL}/index.html"

    override fun run() {
        try {
            println("一级------省.直辖市 解析url:$PROVINCE_URL")
            println("代理ip:${proxyIp.ipAddress},端口:${proxyIp.port}")
            val provinceDoc = Jsoup.connect(PROVINCE_URL)
                    .proxy(proxyIp.ipAddress, proxyIp.port.toInt())
                    .timeout(CONNECTION_TIME_OUT)
                    .get()
            val proElements = provinceDoc.body().getElementsByClass("provincetr")
            val provinces = mutableListOf<CityModel>()
            if (proElements.isNotEmpty()) {
                for (element in proElements) {
                    try {
                        val aTags = element.getElementsByTag("a")
                        if (!aTags.isEmpty()) {
                            for (aTag in aTags) {
                                val name = aTag.html()?.replace("<br>", "") ?: ""
                                val url = aTag.attr("href")
                                url?.apply {
                                    val model = url.split(".")
                                    val code = model[0]
                                    provinces.add(CityModel("", name, code, "", "", url))
                                }
                            }
                        }
                    } catch (e: Exception) {
                        println("ProvinceRunnable.run:解析错误---${e.message}")
                    }
                }
            }
            listener?.onSuccess(provinces)
        } catch (e: Exception) {
            listener?.onFailed(e.message ?: "")
        }
    }
}