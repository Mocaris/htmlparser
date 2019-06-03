## 网页数据爬取,不定期更新
### 一.[🇨🇳中国城市数据爬取(五级联动)](src/main/java/com/mocaris/ChineseCityParser.kt)
同步[gitee链接](https://gitee.com/Mocaris/htmlparser)  
同步[github链接](https://github.com/Mocaris/htmlparser)  
参考  
 ### ChineseCityParser
```kotlin
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
```
数据格式 导出可自行修改
```kotlin
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

```