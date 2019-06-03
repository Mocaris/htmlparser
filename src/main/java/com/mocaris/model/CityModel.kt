package com.mocaris.model


/**
 *
 * 城市信息
 * @Author mocaris
 * @Date 2019/6/1-18:30
 */
class CityModel(val parent_code: String,//上级
                val name: String,//名称
                val statistics_code: String,//统计用代码
                val classification_code: String,//城乡分类代码
                val parent_url_code: String,//上级链接code
                val childUrlSuffix: String//下级区域链接
) {

    fun getInsertSQL(): String {
        return "INSERT INTO tb_citys (parent_code,name,statistics_code,classification_code) VALUE ($parent_code,$name,$statistics_code,$classification_code)"
    }

    override fun toString(): String {
        return "parent_code='$parent_code', name='$name', statistics_code='$statistics_code', classification_code='$classification_code', parent_url_code='$parent_url_code', childUrlSuffix='$childUrlSuffix' \n"
    }
}