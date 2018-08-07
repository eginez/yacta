package com.eginez.yacta.resources.oci

import com.eginez.yacta.resources.Resource
import com.oracle.bmc.Region
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.core.VirtualNetworkClient
import com.oracle.bmc.identity.model.AvailabilityDomain
import com.oracle.bmc.objectstorage.ObjectStorageClient

@DslMarker
annotation class ResourceMarker


val executionGraph : MutableList<Resource> = mutableListOf()


@ResourceMarker
class Oci {

    var profile = "DEFAULT"
    var filePath = "~/.oci/config"
    var provider = ConfigFileAuthenticationDetailsProvider(filePath, profile)
    val DEFAULT_REGION = Region.US_PHOENIX_1
    var region: Region? = null


    fun objectStorage(fn: Oci.() -> Unit) {
        fn()
        println(executionGraph)
        executionGraph.forEach { it.create() }
    }

    fun bucket(fn: BucketResource.() -> Unit) {
        val client = ObjectStorageClient(provider)
        client.setRegion(region)
        val n = BucketResource(client)
        executionGraph.add(n)
        n.apply(fn)
    }

    fun vcn(fn: VcnResource.() -> Unit): Resource {
        val client = VirtualNetworkClient(provider)
        client.setRegion(region)
        val v = VcnResource(client)
        v.apply(fn)
        return v
    }

    fun instance(fn: InstanceResource.() -> Unit): Resource {
        val v = InstanceResource(provider, region)
        v.apply(fn)
        return v
    }
}



