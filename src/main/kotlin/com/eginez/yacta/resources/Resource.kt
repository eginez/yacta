package com.eginez.yacta.resources

import java.util.logging.Logger
import kotlin.reflect.KProperty


interface Resource {
    @Throws(Exception::class)
    fun id(): String

    @Throws(Exception::class)
    fun create()

    //@Throws(Exception::class)
    //fun prepare()

    @Throws(Exception::class)
    fun destroy()

    @Throws(Exception::class)
    fun get(): Resource

    @Throws(Exception::class)
    fun update()

    @Throws(Exception::class)
    fun dependencies(): List<Resource>

}

class LoggerDelegate {

    private var logger: Logger? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Logger {
        if (logger == null) logger = Logger.getLogger(thisRef!!.javaClass.name)
        return logger!!
    }

}

fun logger() = LoggerDelegate()
