package xyz.eginez.yacta.data

import java.util.logging.Logger
import kotlin.reflect.KProperty


interface Resource {
    fun id(): String
    fun dependencies(): List<Resource>
}

interface DataProvider<out T> {
    fun get(): T
}

interface Provisioner<T> {
    fun doCreate(resource: Resource)
    fun doDestroy(resource: Resource)
    fun doUpdate(resource: Resource)
    fun doGet(resource: Resource): T
}


class LoggerDelegate {

    private var logger: Logger? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Logger {
        if (logger == null) logger = Logger.getLogger(thisRef!!.javaClass.name)
        return logger!!
    }

}


fun logger() = LoggerDelegate()
