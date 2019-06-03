package com.mocaris.parser.city

import com.mocaris.ChineseCityParser
import com.mocaris.model.*
import com.mocaris.parser.BaseParser
import org.jsoup.*


/**
 * 四级
 * 乡镇
 * @Author mocaris
 * @Date 2019/6/1-22:19
 */
class TownParser(var proxyIp: ProxyIp, private val countyModel: CityModel) : BaseParser<CityModel>() {

    private val COUNTY_URL = "${ChineseCityParser.BASE_URL}/${countyModel.parent_url_code}/${countyModel.childUrlSuffix}"

    override fun run() {
        if (countyModel.childUrlSuffix.isEmpty()) return@run
        try {
//            Thread.sleep(DELAY_TIME)
            println("四级---${countyModel.name}---乡镇 解析url:$COUNTY_URL")
            println("代理ip:${proxyIp.ipAddress},端口:${proxyIp.port}")
            val countyDoc = Jsoup.connect(COUNTY_URL)
                    .proxy(proxyIp.ipAddress, proxyIp.port.toInt())
                    .timeout(CONNECTION_TIME_OUT)
                    .get()
            val cityTable = countyDoc.getElementsByClass("towntable")
            val cityList = mutableListOf<CityModel>()
            if (cityTable.isNotEmpty()) {
                //县
                val countyCode = countyModel.childUrlSuffix.split(".")[0].split("/")[0]
                val cityTr = cityTable[0].getElementsByClass("towntr")
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
                            cityList.add(CityModel(countyModel.statistics_code, name, code, "", "${countyModel.parent_url_code}/$countyCode", href))
                        } else {
                            val cityTds = city.getElementsByTag("td")
                            if (cityTds.isNotEmpty()) {
                                val codeTag = cityTds[0]//第一个是代号
                                val nameTag = cityTds[1]//第二个是名称
                                val code = codeTag.html()
                                val name = nameTag.html()
                                cityList.add(CityModel(countyModel.statistics_code, name, code, "", "${countyModel.parent_url_code}/$countyCode", ""))
                            }
                        }
                    } catch (e: Exception) {
                        println("TownRunnable.run:解析错误---${e.message}")
                    }
                }
            }
            listener?.onSuccess(cityList)
        } catch (e: Exception) {
            listener?.onFailed(e.message ?: "")
        }

    }
}