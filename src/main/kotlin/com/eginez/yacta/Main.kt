package com.eginez.yacta

import com.eginez.yacta.resources.oci.AvailabilityDomains
import com.eginez.yacta.resources.oci.Oci
import com.eginez.yacta.resources.oci.VcnResource


val COMPARTMET = "ocidv1:tenancy:oc1:phx:1460406592660:aaaaaaaab4faofrfkxecohhjuivjq262pu"
fun main(args: Array<String>){
    println("Starting")

    val oci = Oci()
    oci.region = oci.DEFAULT_REGION
    val vcnOne = createVcn(oci)

    val availDomain = AvailabilityDomains(oci.provider).get().first()

    val instance = oci.instance {
        availabilityDomain = availDomain
        compartment = COMPARTMET
        displayName = "DSLInstance"

        vnic {
            publicIp = false
            name = "privateNic"
            subnet {
                availabilityDomain = availDomain
                vcn = vcnOne
            }
        }
    }

    instance.create()
}

fun createVcn(oci: Oci): VcnResource {
    val vcn = oci.vcn{
        displayName = "VcnFromDSL"
        compartmentId = COMPARTMET
        cidrBlock = "172.0.0.0/16"
        dnsLabel = displayName
    }
    vcn.create()
    println("Vnc created: $vcn")
    return vcn as VcnResource
}
