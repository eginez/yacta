package com.eginez.yacta

import com.eginez.yacta.resources.oci.*
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngine
import javax.script.ScriptContext
import javax.script.ScriptEngineManager
import kotlin.system.measureTimeMillis


val COMPARTMET_ID = "ocid1.compartment.oc1..aaaaaaaaptqakzgdmjxr4oq6f6v3vtoc5t3j44frmjf6snlm5zgfwo6lwkua"
fun main(args: Array<String>) {
    println("Starting")

    val oci = Oci()
    oci.region = oci.DEFAULT_REGION

    val homeCompartment = compartment { id = COMPARTMET_ID }

    val vcnOne = oci.vcn {
        displayName = "VcnFromDSL"
        compartment = homeCompartment
        cidrBlock = "10.0.0.0/16"
    }

    val availDomains = AvailabilityDomains(oci.provider)
    val imagesSource = ComputeImages(oci.provider)
    val shapes = ComputeShapes(oci.provider)

    shapes.compartment = homeCompartment
    imagesSource.compartment = homeCompartment
    availDomains.compartment = homeCompartment
    val images = imagesSource.get()

    val firstAvailabilityDomain = availDomains.get().first()


    val instance = oci.instance {
        //TODO finish implementing this fun
        availabilityDomain = firstAvailabilityDomain
        compartment = homeCompartment
        displayName = "DSLInstance"
        image = images.find { it.displayName.contains("Canonical-Ubuntu", true)
                && !it.displayName.contains("GPU", true)  }!!
        shape = shapes.get().find { it.shape.contains("VM.Standard1.2", true)}!!

        vnic = vnic {
            publicIp = true
            subnet {
                cidrBlock = "10.0.0.0/24"
                availabilityDomain = firstAvailabilityDomain
                compartment = homeCompartment
                vcn = vcnOne
                name = "DSLSubnet"
            }
        }
    }

    instance.create()
    println(instance)
    //vcnOne.destroy()
    //runFromScript()
}

fun runFromScript(): Unit {
    var engineByExtension = ScriptEngineManager().getEngineByExtension("kts")!!
    var se = engineByExtension as KotlinJsr223JvmLocalScriptEngine
   val eval = se.eval("""
            package com.sample.one
            import com.eginez.yacta.resources.oci.*

            val oci = Oci()
            oci
            """)
    println(eval)
    val oci2 = eval as Oci
    println(oci2.filePath)


}

