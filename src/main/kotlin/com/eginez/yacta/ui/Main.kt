package com.eginez.yacta.ui

import com.eginez.yacta.plugin.oci.Oci
import com.eginez.yacta.plugin.oci.compartment
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngine
import picocli.CommandLine
import picocli.CommandLine.Command
import java.io.File
import javax.script.ScriptEngineManager


val COMPARTMET_ID = "ocid1.compartment.oc1..aaaaaaaaptqakzgdmjxr4oq6f6v3vtoc5t3j44frmjf6snlm5zgfwo6lwkua"
@Command(name="yacta", version = ["0.1"], description = ["build infrastructure via dsls"] )
class MainCli: Runnable {

    @CommandLine.Parameters(arity = "1", description = ["script file containing infrastructure definition"])
    private var infraScriptFile: File? = null

    private val scriptEngine =
            ScriptEngineManager().getEngineByExtension("kts") as KotlinJsr223JvmLocalScriptEngine


    override fun run() {
        println(infraScriptFile?.absolutePath)
        println("Compiling script: ${infraScriptFile?.name}")
        val infra = scriptEngine.eval(infraScriptFile?.reader())
        println(infra)

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
}

fun main(args: Array<String>) {
    CommandLine.run(MainCli(), *args)

    /*
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
        image = images.find {
            it.displayName.contains("Canonical-Ubuntu", true)
                    && !it.displayName.contains("GPU", true)
        }!!
        shape = shapes.find { it.shape.contains("VM.Standard1.2", true) }!!

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
    */
}

