package com.mocaris.parser.city

import com.mocaris.ChineseCityParser
import com.mocaris.model.*
import com.mocaris.parser.BaseParser
import org.jsoup.*


/**
 * 二级
 *  市区
 * @Author mocaris
 * @Date 2019/6/1-22:19
 * @param provinceCode 省级 直辖市 统计代码
 */
class CityParser(var proxyIp: ProxyIp, private val parent: CityModel) : BaseParser<CityModel>() {

    private val CITY_URL = "${ChineseCityParser.BASE_URL}/${parent.childUrlSuffix}"

    override fun run() {
        val childUrlSuffix = parent.childUrlSuffix
        if (childUrlSuffix.isEmpty()) return@run
        try {
//            Thread.sleep(DELAY_TIME)
            println("二级---${parent.name}---市 区 解析url:$CITY_URL")
            println("代理ip:${proxyIp.ipAddress},端口:${proxyIp.port}")
            val countyDoc = Jsoup.connect(CITY_URL)
                    .proxy(proxyIp.ipAddress, proxyIp.port.toInt())
                    .timeout(CONNECTION_TIME_OUT)
                    .get()
            val cityTable = countyDoc.getElementsByClass("citytable")
            val cityList = mutableListOf<CityModel>()
            if (cityTable.isNotEmpty()) {
                val cityTr = cityTable[0].getElementsByClass("citytr")
                //解析市区
                for (city in cityTr) {
                    try {
                        //先获取城市a标签信息,没有说明没有下级城市
                        val aTags = city.getElementsByTag("a")
                        if (aTags.isNotEmpty()) {
                            val codeTag = aTags[0]//第一个是代号
                            val nameTag = aTags[1]//第二个是名称
                            val href = codeTag.attr("href")//链接
                            val code = codeTag.html()
                            val name = nameTag.html()
                            cityList.add(CityModel(parent.statistics_code, name, code, "", "", href))
                        } else {
                            val cityTds = city.getElementsByTag("td")
                            if (cityTds.isNotEmpty()) {
                                val codeTag = cityTds[0]//第一个是代号
                                val nameTag = cityTds[1]//第二个是名称
                                val code = codeTag.html()
                                val name = nameTag.html()
                                cityList.add(CityModel(parent.statistics_code, name, code, "", "", ""))
                            }
                        }
                    } catch (e: Exception) {
                        println("CityRunnable.run:解析错误---${e.message}")
                    }
                }
            }
            listener?.onSuccess(cityList)
        } catch (e: Exception) {
            listener?.onFailed(e.message ?: "")
        }

    }
}