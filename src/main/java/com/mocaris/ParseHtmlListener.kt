package com.mocaris


/**
 *
 * @Author mocaris
 * @Date 2019/6/1-18:02
 */
interface ParseHtmlListener<Model> {
    fun onSuccess(models: MutableList<Model>)
    fun onFailed(erMsg: String)
}