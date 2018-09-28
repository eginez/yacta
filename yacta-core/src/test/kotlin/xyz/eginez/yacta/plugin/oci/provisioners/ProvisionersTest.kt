package xyz.eginez.yacta.plugin.oci.provisioners

import com.oracle.bmc.Region
import org.junit.Test
import xyz.eginez.yacta.plugin.oci.Oci
import xyz.eginez.yacta.plugin.oci.availabilityDomains
import xyz.eginez.yacta.plugin.oci.vcn


class ProvisionersTest {
    val oci = Oci(region = Region.US_PHOENIX_1,
            compartmentId = "ocidv1:tenancy:oc1:phx:1460406592660:aaaaaaaab4faofrfkxecohhjuivjq262pu",
            configFilePath = "~/.oraclebmc/config")

    @Test
    fun simpleTest() {
        val v = oci.vcn {
            cidrBlock = availabilityDomains.first().name
        }

        println(ProvsionToTF(v))
    }
}