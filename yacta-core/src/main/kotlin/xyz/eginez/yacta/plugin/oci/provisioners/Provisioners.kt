package xyz.eginez.yacta.plugin.oci.provisioners

import xyz.eginez.yacta.plugin.oci.VcnResource
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.KVisibility
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.withNullability



class TFProperty<T, R>(val instance: Any?, val prop: KProperty1<T, R>) {
    override fun toString(): String {
        if (instance == null) {
            return ""
        }
        val vv = prop.get(instance as T)
        return """${prop.name} = "${vv}""""
    }
}

class TFResource {
    lateinit var name: String
    lateinit var properties: List<TFProperty<*, *>>
}


fun createName(): String {
    return "some-name"
}

fun KType.isPrimitive(): Boolean {
    val nonNull = this.withNullability(false)
    return Number::class.starProjectedType == nonNull
            || Number::class.allSupertypes.contains(nonNull)
            || String::class.starProjectedType == nonNull
            || String::class.allSupertypes.contains(nonNull)
}

fun toTFRes(r: VcnResource): TFResource {
    val res = TFResource()
    val ll = mutableListOf<TFProperty<*, *>>()
    res.name = "oci_core_virtual_network"
    r::class.memberProperties.filter {
        it.visibility == KVisibility.PUBLIC &&
                it.returnType.isPrimitive()
    }.forEach { ll.add(TFProperty(r, it)) }
    res.properties = ll
    return res
}

fun ProvsionToTF(r: VcnResource): String {
    val res = toTFRes(r)

    return """
resource ${res.name} "${createName()}" {
    ${res.properties.forEachString {
        it.toString()
    }}
}
"""
}

fun <T> List<T>.forEachString(fn: (T) -> String): String {
    val sb = StringBuilder()
    this.forEach {
        sb.append(fn(it))
        sb.append("\n")
    }
    return sb.toString()
}



