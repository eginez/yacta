package sampleScripts

import com.eginez.yacta.plugin.oci.Oci
import com.eginez.yacta.plugin.oci.compartment
import com.oracle.bmc.Region

//val compartmendId = System.getenv("COMPARTMENT_ID")
val compartmendId = "ocid1.compartment.oc1..aaaaaaaaptqakzgdmjxr4oq6f6v3vtoc5t3j44frmjf6snlm5zgfwo6lwkua"
val oci = Oci(region = Region.US_PHOENIX_1,
        compartmentId = compartmendId,
        configFilePath = "~/.oraclebmc/config")
val homeCompartment = compartment { id = compartmendId }

val availDomains = oci.availabilityDomains()
val images = oci.computeImages(homeCompartment)
val shapes = oci.computeShapes(homeCompartment)


val firstAvailabilityDomain = availDomains.first()

oci.instance {
    sshPublicKey = System.getenv("SSH_PUBLIC_KEY")
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


