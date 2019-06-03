package com.mocaris.parser

import com.mocaris.*


/**
 *
 * @Author mocaris
 * @Date 2019/6/1-22:13
 */
abstract class BaseParser<Model> : Runnable {

    companion object {
        const val CONNECTION_TIME_OUT: Int = 10000;
        const val DELAY_TIME: Long = 2000;
    }


    var listener: ParseHtmlListener<Model>? = null
}