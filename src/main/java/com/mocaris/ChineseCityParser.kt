package com.mocaris

import com.mocaris.model.*
import com.mocaris.parser.city.*
import java.io.*
import java.util.concurrent.*
import kotlin.random.*


/**
 *  中国城市爬取
 *  [http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2018/index.html]
 * @Author mocaris
 * @Date 2019/6/1-17:55
 */
class ChineseCityParser {

    companion object {

        const val BASE_URL = "http://www.stats.gov.cn/tjsj/tjbz/tjyqhdmhcxhfdm/2018"

        val threadExecutor = ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() * 2,
            Int.MAX_VALUE,
            20000,
            TimeUnit.MILLISECONDS,
            LinkedBlockingQueue()
        )


        @Volatile
        private var cityCount = 0

        var IP_PROXYS: MutableList<ProxyIp> = mutableListOf()

        @JvmStatic
        fun main(args: Array<String>) {
            val proxyParser = ProxyParser()
            proxyParser.listener = object : ParseHtmlListener<ProxyIp> {
                override fun onSuccess(ipProxys: MutableList<ProxyIp>) {
                    IP_PROXYS = ipProxys
                    println("代理ip:\n${ipProxys.toString()}")
                    val parser = ChineseCityParser()
                    parser.parseProvinceHtml()
                }

                override fun onFailed(erMsg: String) {
                    threadExecutor.execute(proxyParser)
                }

            }
            threadExecutor.execute(proxyParser)
        }

        fun getRandomProxyIp(): ProxyIp {
            val random = Random.nextInt(IP_PROXYS.size)
            return IP_PROXYS[random]
        }
    }

    /**
     * 一级
     * 省 直辖市
     */
    fun parseProvinceHtml() {
        val provinceThread = ProvinceParser(getRandomProxyIp())
        provinceThread.listener = object : ParseHtmlListener<CityModel> {
            override fun onSuccess(citys: MutableList<CityModel>) {
                cityCount += citys.size
                println("省份/直辖市=====获取完成===数量$cityCount===================")
                writeFile(null, citys)
                for (province in citys) {
                    if (province.childUrlSuffix.isNotEmpty()) {
                        parseCityHtml(province)
                    }
                }
            }

            override fun onFailed(erMsg: String) {
                println("erMsg = [$erMsg]")
                provinceThread.proxyIp = getRandomProxyIp()
                threadExecutor.execute(provinceThread)
            }
        }
        threadExecutor.execute(provinceThread)
    }

    /**
     * 二级
     * 市 区
     */
    fun parseCityHtml(province: CityModel) {
        val cityRunnable = CityParser(getRandomProxyIp(), province)
        cityRunnable.listener = object : ParseHtmlListener<CityModel> {
            override fun onSuccess(citys: MutableList<CityModel>) {
                cityCount += citys.size
                println("${province.name}=====数量$cityCount======================")
                writeFile(province, citys)
                for (city in citys) {
                    if (city.childUrlSuffix.isNotEmpty()) {
                        parseCountyHtml(city)
                    }
                }
            }

            override fun onFailed(erMsg: String) {
                println("erMsg = [$erMsg]")
                cityRunnable.proxyIp = getRandomProxyIp()
                threadExecutor.execute(cityRunnable)
            }

        }
        threadExecutor.execute(cityRunnable)
    }

    /**
     * 三级
     * 区 县
     */
    fun parseCountyHtml(cityModel: CityModel) {
        val countyRunnable = CountyParser(getRandomProxyIp(), cityModel)
        countyRunnable.listener = object : ParseHtmlListener<CityModel> {
            override fun onSuccess(citys: MutableList<CityModel>) {
                cityCount += citys.size
                println("${cityModel.name}======数量:$cityCount=====================")
                writeFile(cityModel, citys)
                for (county in citys) {
                    if (county.childUrlSuffix.isNotEmpty()) {
                        parseTownHtml(county)
                    }
                }
            }

            override fun onFailed(erMsg: String) {
                println("erMsg = [$erMsg]")
                countyRunnable.proxyIp = getRandomProxyIp()
                threadExecutor.execute(countyRunnable)
            }

        }
        threadExecutor.execute(countyRunnable)
    }

    /**
     * 四级
     * 下级需要加上本级的code
     * 乡镇
     */
    fun parseTownHtml(countyModel: CityModel) {
        val townRunnable = TownParser(getRandomProxyIp(), countyModel)
        townRunnable.listener = object : ParseHtmlListener<CityModel> {
            override fun onSuccess(citys: MutableList<CityModel>) {
                cityCount += citys.size
                println("${countyModel.name}=====数量:$cityCount======================")
                writeFile(countyModel, citys)
                for (town in citys) {
                    if (town.childUrlSuffix.isNotEmpty()) {
                        parseVillageHtml(town)
                    }
                }
            }

            override fun onFailed(erMsg: String) {
                println("erMsg = [$erMsg]")
                townRunnable.proxyIp = getRandomProxyIp()
                threadExecutor.execute(townRunnable)
            }

        }
        threadExecutor.execute(townRunnable)
    }

    /**
     * 五级
     * 需要加上 四级 parent_code 和本级 parent_code
     * 村  居委会
     */
    fun parseVillageHtml(townModel: CityModel) {
        val villageRunnable = VillageParser(getRandomProxyIp(), townModel)
        villageRunnable.listener = object : ParseHtmlListener<CityModel> {
            override fun onSuccess(citys: MutableList<CityModel>) {
                cityCount += citys.size
                println("${townModel.name}=========数量:$cityCount==================")
                writeFile(townModel, citys)
            }

            override fun onFailed(erMsg: String) {
                println("erMsg = [$erMsg]")
                villageRunnable.proxyIp = getRandomProxyIp()
                threadExecutor.execute(villageRunnable)
            }

        }
        threadExecutor.execute(villageRunnable)
    }

    private val outputStream: FileOutputStream = FileOutputStream("city_log.txt", true)
    /*
    * INSERT INTO tb_cities (parent_code,name,statistics_code,classification_code) VALUES
    * */
    fun writeFile(parent: CityModel?, models: List<CityModel>?) {
        models ?: return
//        threadExecutor.execute {
        synchronized(this::class.java) {
            val file = File("city_log.txt")
            if (!file.exists()) {
                file.createNewFile()
            }
            if (file.length() <= 0) {
                outputStream.write("新建表名 tb_cities 可直接复制粘贴下面 sql 语句执行\n".toByteArray(Charsets.UTF_8))
                outputStream.write(
                    "CREATE TABLE tb_cities (_id INTEGER PRIMARY KEY AUTOINCREMENT,parent_code TEXT,name TEXT,statistics_code TEXT,classification_code TEXT)\n".toByteArray(
                        Charsets.UTF_8
                    )
                )
                outputStream.write(
                    "INSERT INTO tb_cities (parent_code,name,statistics_code,classification_code) VALUES \n".toByteArray(
                        Charsets.UTF_8
                    )
                )
                outputStream.flush()
            }
            val sqlStr = StringBuilder()
            for (city in models) {
                sqlStr.append(" ('${city.parent_code}','${city.name}','${city.statistics_code}','${city.classification_code}')")
                sqlStr.append(",\n")
            }
            val sql = sqlStr.toString()
            outputStream.write(sql.toByteArray(Charsets.UTF_8))
            outputStream.flush()
            println(sql)
        }
//        }
    }

}