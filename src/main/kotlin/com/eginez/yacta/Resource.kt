package com.eginez.yacta

import com.oracle.bmc.Region
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.core.ComputeClient
import com.oracle.bmc.core.model.LaunchInstanceDetails
import com.oracle.bmc.core.requests.LaunchInstanceRequest

@DslMarker
annotation class ResourceMarker

@ResourceMarker
class Resource {

    var computeClient: ComputeClient? = null
    val builder = LaunchInstanceDetails.Builder()
    var availabiltyDomains: String = ""
    var compartmentId: String = ""

    fun compute(fn: Resource.() -> Unit): Unit {
        this.computeClient = createComputeClient()
        createCompute()
    }

    fun vnicDetails(fn: Resource.() -> Unit): Unit {


    }


    private fun createComputeClient(): ComputeClient {
        var profile = "DEFAULT"
        var filePath = "~/.oraclebmc/config"
        var provider = ConfigFileAuthenticationDetailsProvider(filePath, profile)
        var client = ComputeClient(provider)
        client.setRegion(Region.US_ASHBURN_1)
        return client

    }

    private fun createCompute() {
        val builder = LaunchInstanceRequest.builder()
    }
}

fun main(args: Array<String>) {
    val c = Resource()
    c.compute {
        availabiltyDomains = "asdf"
        compartmentId = "asdf"
        vnicDetails {

        }
    }
}