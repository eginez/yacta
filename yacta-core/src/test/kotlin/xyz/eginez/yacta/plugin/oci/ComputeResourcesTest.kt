package xyz.eginez.yacta.plugin.oci

import com.oracle.bmc.Region
import org.junit.Ignore
import org.junit.Test
import java.nio.file.Paths

class ComputeResourcesTest {
    val configLocation = Paths.get("~/.oci", "config")

    @Test
    @Ignore
    fun createInstance() {

        val compartmentId = System.getenv("COMPARTMENT_ID")
        val oci = Oci(region = Region.US_PHOENIX_1,
                compartmentId = compartmentId,
                configFilePath = configLocation.toString())


        val homeCompartment = oci.compartment
        val instance = oci.instance {
            sshPublicKey = System.getenv("SSH_PUBLIC_KEY")
            availabilityDomain = availabilityDomains.first()
            displayName = "DSLInstance"

            image = image(osName="Canonical Ubuntu", osVersion="16.04", gpu=false)

            shape = shape(name="Standard1.4", vm=true)

            val vnetwork = oci.vcn {
                displayName = "VcnFromDSL"
                cidrBlock = "10.0.0.0/16"
            }

            val ig = oci.internetGateway {
                displayName = "DSLInternetGateway"
                enabled = true
                vcn = vnetwork
            }

            vnetwork.routeTable {
                rule("0.0.0.0/0", ig)
            }

            vnic = vnic {
                publicIp = true
                subnet {
                    cidrBlock = "10.0.0.0/24"
                    availabilityDomain = availabilityDomains.first()
                    compartment = homeCompartment
                    name = "DSLSubnet"
                    vcn = vnetwork
                }
            }
        }

        instance.create()
        instance.publicIp()
    }
}
