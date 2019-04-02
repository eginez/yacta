package xyz.eginez.yacta.plugin.oci

import com.oracle.bmc.Region
import com.oracle.bmc.identity.model.Compartment
import org.junit.Test
import xyz.eginez.yacta.core.exportInit
import xyz.eginez.yacta.core.provision
import java.io.StringWriter

class EngineTest {
    val oci = Oci(region = Region.US_PHOENIX_1,
            compartmentId = "ocidv1:tenancy:oc1:phx:1460406592660:aaaaaaaab4faofrfkxecohhjuivjq262pu")

    @Test
    fun twoCompartments() {
        val c = oci.compartment{
            name = "right here"
            description = "some descroption here"

            (1..3).forEach { it ->
                compartment {
                    name = "name ${it}"
                    description = "description 2"
                }
            }

            vcn {
                displayName = "some vcn"
                cidrBlock = "10.0.0.0/16"
            }
        }

        val w = provision(c)
        println(w.toString())

    }

    @Test
    fun testImport() {
        val w = exportInit(oci.tenancy!!)
        println(w.toString())

    }

}