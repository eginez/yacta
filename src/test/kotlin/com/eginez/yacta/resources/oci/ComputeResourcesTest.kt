package com.eginez.yacta.resources.oci

import com.oracle.bmc.Region
import org.junit.Test

class ComputeResourcesTest {

    @Test
    fun createInstance() {

        val COMPARTMET_ID = "ocid1.compartment.oc1..aaaaaaaaptqakzgdmjxr4oq6f6v3vtoc5t3j44frmjf6snlm5zgfwo6lwkua"
        val oci = Oci(region = Region.US_PHOENIX_1,
                compartmentId = COMPARTMET_ID,
                configFilePath = "~/.oraclebmc/config")
        val homeCompartment = compartment { id = COMPARTMET_ID }

        val availDomains = oci.availabilityDomains()
        val images = oci.computeImages(homeCompartment)
        val shapes = oci.computeShapes(homeCompartment)


        val firstAvailabilityDomain = availDomains.first()

        val instance = oci.instance {
            sshPublicKey = System.getenv("SSH_PUBLIC_KEY")
            availabilityDomain = firstAvailabilityDomain
            compartment = homeCompartment
            displayName = "DSLInstance"

            image = images.find {
                it.displayName.contains("Canonical-Ubuntu", true)
                        && !it.displayName.contains("GPU", true)
            }!!

            shape = shapes.find { it.shape.contains("VM.Standard1.2", true) }!!

            val vnetwork = oci.vcn {
                displayName = "VcnFromDSL"
                compartment = homeCompartment
                cidrBlock = "10.0.0.0/16"
            }

            val ig = oci.internetGateway {
                displayName = "DSLInternetGateway"
                enabled = true
                compartment = homeCompartment
                vcn = vnetwork
            }

            vnetwork.routeTable {
                rule("0.0.0.0/0", ig)
            }

            vnic = vnic {
                publicIp = true
                subnet {
                    cidrBlock = "10.0.0.0/24"
                    availabilityDomain = firstAvailabilityDomain
                    compartment = homeCompartment
                    name = "DSLSubnet"
                    vcn = vnetwork
                }
            }
        }

        instance.create()
    }
}