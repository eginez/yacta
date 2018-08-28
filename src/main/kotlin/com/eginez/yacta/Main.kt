package com.eginez.yacta

import com.eginez.yacta.resources.oci.*
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngine
import javax.script.ScriptEngineManager


val COMPARTMET_ID = "ocid1.compartment.oc1..aaaaaaaaptqakzgdmjxr4oq6f6v3vtoc5t3j44frmjf6snlm5zgfwo6lwkua"
fun main(args: Array<String>) {
    println("Starting")

    val oci = Oci(Oci.DEFAULT_REGION, compartmentId = COMPARTMET_ID)

    val homeCompartment = compartment { id = COMPARTMET_ID }

    val availDomains = oci.availabilityDomains()
    val images = oci.computeImages(homeCompartment)
    val shapes = oci.computeShapes(homeCompartment)


    val firstAvailabilityDomain = availDomains.first()

    val vcnOne = oci.vcn {
        displayName = "VcnFromDSL"
        compartment = homeCompartment
        cidrBlock = "10.0.0.0/16"
    }


    val instance = oci.instance {
        //TODO finish implementing this fun
        availabilityDomain = firstAvailabilityDomain
        compartment = homeCompartment
        displayName = "DSLInstance"
        image = images.find { it.displayName.contains("Canonical-Ubuntu", true)
                && !it.displayName.contains("GPU", true)  }!!
        shape = shapes.find { it.shape.contains("VM.Standard1.2", true)}!!

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
    println(oci2.configFilePath)


}

