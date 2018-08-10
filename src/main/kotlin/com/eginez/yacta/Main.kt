package com.eginez.yacta

import com.eginez.yacta.resources.oci.*


val COMPARTMET_ID = "ocid1.compartment.oc1..aaaaaaaaptqakzgdmjxr4oq6f6v3vtoc5t3j44frmjf6snlm5zgfwo6lwkua"
fun main(args: Array<String>){
    println("Starting")

    val oci = Oci()
    oci.region = oci.DEFAULT_REGION

    val homeCompartment =  compartment {
        id = COMPARTMET_ID
    }

    val vcnOne = oci.vcn{
        displayName = "VcnFromDSL"
        compartment = homeCompartment
        cidrBlock = "10.0.0.0/16"
        dnsLabel = displayName
    }

    val availDomains = AvailabilityDomains(oci.provider)
    availDomains.compartment = homeCompartment
    val firstAvailabilityDomain = availDomains.get().first()


    val instance = oci.instance {
        //TODO finish implementing this fun
        availabilityDomain = firstAvailabilityDomain
        compartment = homeCompartment
        displayName = "DSLInstance"
        image = ""
        hostLabel = "theHost"
        shape = ""

        vnic = vnic {
            publicIp = false
            name = "privateNic"
            subnet {
                cidrBlock = "10.0.0.0/24"
                availabilityDomain = firstAvailabilityDomain
                compartment = homeCompartment
                vcn = vcnOne
            }
        }
    }

    instance.create()
    vcnOne.destroy()
}

