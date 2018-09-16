package sampleScripts

import xyz.eginez.yacta.data.asFile
import xyz.eginez.yacta.plugin.oci.Oci
import xyz.eginez.yacta.plugin.oci.compartment
import com.oracle.bmc.Region

val compartmentId = System.getenv("COMPARTMENT_ID")
check(compartmentId != null && compartmentId.isNotBlank(),
        {"COMPARTMENT_ID needs to be set as part of environment"})

val oci = Oci(region = Region.US_PHOENIX_1,
        compartmentId = compartmentId,
        configFilePath = "~/.oraclebmc/config")
val homeCompartment = compartment { id = compartmentId }

val availDomains = oci.availabilityDomains()
val images = oci.computeImages(homeCompartment)
val shapes = oci.computeShapes(homeCompartment)


val firstAvailabilityDomain = availDomains.first()

oci.instance {
    sshPublicKey = "~/.ssh/id_rsa.pub".asFile().readText()
    availabilityDomain = firstAvailabilityDomain
    displayName = "DSLInstance"

    image = images.find {
        it.displayName.contains("Canonical-Ubuntu", true)
                && !it.displayName.contains("GPU", true)
    }!!

    shape = shapes.find { it.shape.contains("VM.Standard1.2", true) }!!

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
            availabilityDomain = firstAvailabilityDomain
            compartment = homeCompartment
            name = "DSLSubnet"
            vcn = vnetwork
        }
    }
}


