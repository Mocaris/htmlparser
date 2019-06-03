package com.mocaris.parser.city

import com.mocaris.ChineseCityParser
import com.mocaris.model.*
import com.mocaris.parser.BaseParser
import org.jsoup.*


/**
 * 五级
 * 村
 * @Author mocaris
 * @Date 2019/6/1-22:19
 */
class VillageParser(var proxyIp: ProxyIp, private val villageModel: CityModel) : BaseParser<CityModel>() {

    private val COUNTY_URL = "${ChineseCityParser.BASE_URL}/${villageModel.parent_url_code}/${villageModel.childUrlSuffix}"

    override fun run() {
        if (villageModel.childUrlSuffix.isEmpty()) return@run
        try {
//            Thread.sleep(DELAY_TIME)
            println("五级--${villageModel.name}---村 解析url:$COUNTY_URL")
            println("代理ip:${proxyIp.ipAddress},端口:${proxyIp.port}")
            val countyDoc = Jsoup.connect(COUNTY_URL)
                    .proxy(proxyIp.ipAddress, proxyIp.port.toInt())
                    .timeout(CONNECTION_TIME_OUT)
                    .get()
            val cityTable = countyDoc.getElementsByClass("villagetable")
            val cityList = mutableListOf<CityModel>()
            if (cityTable.isNotEmpty()) {
                val villageTr = cityTable[0].getElementsByClass("villagetr")
                //解析村,只到村,没有更多下级
                for (city in villageTr) {
                    try {
                        val cityTds = city.getElementsByTag("td")
                        if (cityTds.isNotEmpty()) {
                            val codeTag = cityTds[0]//第一个是代号
                            val classTag = cityTds[1]//第二个城乡分类代码
                            val nameTag = cityTds[2]//第三个是名称
                            val code = codeTag.html()
                            val classCode = classTag.html()
                            val name = nameTag.html()
                            cityList.add(CityModel(villageModel.statistics_code, name, code, classCode, "", ""))
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