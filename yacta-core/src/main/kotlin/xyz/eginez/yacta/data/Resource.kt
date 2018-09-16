package xyz.eginez.yacta.data

import java.util.logging.Logger
import kotlin.reflect.KProperty


interface Resource<T> {
    fun id(): String

    fun create()

    fun prepare() = {}

    fun destroy()

    fun get(): T

    fun update()

    fun dependencies(): List<*>

}


// interface StatefulResource
// CRUDResource
// interface SharedResource




class LoggerDelegate {

    private var logger: Logger? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Logger {
        if (logger == null) logger = Logger.getLogger(thisRef!!.javaClass.name)
        return logger!!
    }

}

//

fun logger() = LoggerDelegate()
