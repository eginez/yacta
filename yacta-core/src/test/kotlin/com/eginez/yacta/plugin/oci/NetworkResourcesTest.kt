package com.eginez.yacta.plugin.oci

import com.oracle.bmc.Region
import org.junit.Test


class NetworkResourcesTest {
    @Test
    fun simpleVcn() {
        val compartmentId = "testcompartment"
        val oci = Oci(region = Region.US_PHOENIX_1,
                compartmentId = compartmentId,
                configFilePath = "~/.oci/config")

        val v = oci.vcn {
            cidrBlock = availabilityDomains.first().name
        }
        v.create()
    }
}