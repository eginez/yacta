package com.eginez.yacta.resources.oci

import com.eginez.yacta.resources.Resource
import com.oracle.bmc.Region
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.core.VirtualNetworkClient
import com.oracle.bmc.core.model.Image
import com.oracle.bmc.core.model.Shape
import com.oracle.bmc.identity.model.AvailabilityDomain
import com.oracle.bmc.objectstorage.ObjectStorageClient

@DslMarker
annotation class ResourceMarker


val executionGraph : MutableList<Resource<*>> = mutableListOf()


@ResourceMarker
class Oci (val region: Region,
           val compartmentId: String,
           val configFilePath: String = "~/.oci/config",
           profile: String = "DEFAULT") {

    var provider = ConfigFileAuthenticationDetailsProvider(configFilePath, profile)
    private val compartment: CompartmentResource

    companion object { val DEFAULT_REGION = Region.US_PHOENIX_1 }
    init {
        compartment = com.eginez.yacta.resources.oci.compartment { id = compartmentId }
    }


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

    fun vcn(fn: VcnResource.() -> Unit): VcnResource {
        val client = VirtualNetworkClient(provider)
        client.setRegion(region)
        val v = VcnResource(client)
        v.apply(fn)
        return v
    }

    fun instance(fn: InstanceResource.() -> Unit): InstanceResource {
        val v = InstanceResource(provider, region)
        v.apply(fn)
        return v
    }

    fun internetGateway(fn: InternetGatewayResource.() -> Unit): InternetGatewayResource {
        val client = VirtualNetworkClient(provider)
        client.setRegion(region)
        val v = InternetGatewayResource(client)
        v.apply(fn)
        return v
    }




    fun availabilityDomains(compartment: CompartmentResource? = null, region: Region = this.region): Set<AvailabilityDomain> {
        val ads = AvailabilityDomains(provider, region)
        ads.compartment = compartment ?: this.compartment
        return ads.get()
    }

    fun computeImages(compartment: CompartmentResource, region: Region = this.region): Set<Image> {
        val ads = ComputeImages(provider, region)
        ads.compartment = compartment
        return ads.get()
    }

    fun computeShapes(compartment: CompartmentResource, region: Region = this.region): Set<Shape> {
        val ads = ComputeShapes(provider, region)
        ads.compartment = compartment
        return ads.get()
    }

}



