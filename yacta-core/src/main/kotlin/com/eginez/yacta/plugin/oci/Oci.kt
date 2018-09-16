package com.eginez.yacta.plugin.oci

import com.eginez.yacta.data.Resource
import com.eginez.yacta.data.logger
import com.oracle.bmc.Region
import com.oracle.bmc.auth.AuthenticationDetailsProvider
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.core.VirtualNetworkClient
import com.oracle.bmc.core.model.Image
import com.oracle.bmc.core.model.Shape
import com.oracle.bmc.identity.model.AvailabilityDomain
import com.oracle.bmc.objectstorage.ObjectStorageClient

@DslMarker
annotation class ResourceMarker


val executionGraph : MutableList<Resource<*>> = mutableListOf()


abstract class OciBaseResource<T> (val configurationProvider: AuthenticationDetailsProvider,
                                  val region: Region,
                                  var compartment: CompartmentResource?): Resource<T> {

    val LOG by logger()

    //TODO fix dispatch of listeners
    var listeners: MutableSet<ResourceStateChangeListener> = mutableSetOf(LoggerListener(this))
    override fun create() {
        listeners.forEach{it.willCreate()}
        doCreate()
        listeners.forEach{it.didCreate()}
    }
    abstract fun doCreate()

    override fun destroy() {
        listeners.forEach{it.willDestroy()}
        doDestroy()
        listeners.forEach{it.didDestroy()}
    }
    abstract fun doDestroy()

    override fun update() {
        listeners.forEach{it.willUpdate()}
        doUpdate()
        listeners.forEach{it.didUpdate()}
    }
    abstract fun doUpdate()

}


interface ResourceStateChangeListener {
    fun willCreate()
    fun didCreate()
    fun willDestroy()
    fun didDestroy()
    fun willUpdate()
    fun didUpdate()
}

class LoggerListener(private val resource: OciBaseResource<*>): ResourceStateChangeListener {
    override fun willCreate() = resource.LOG.info("will create: [$resource]")
    override fun didCreate() = resource.LOG.info("did create: [$resource]")
    override fun willDestroy() =resource.LOG.info("will destroy: [$resource]")
    override fun didDestroy() = resource.LOG.info("did destroy: [$resource]")
    override fun willUpdate() = resource.LOG.info("will update: [$resource]")
    override fun didUpdate() = resource.LOG.info("did update: [$resource]")
}


@ResourceMarker
class Oci (val region: Region,
           val compartmentId: String,
           val configFilePath: String = "~/.oci/config",
           profile: String = "DEFAULT") {

    var provider = ConfigFileAuthenticationDetailsProvider(configFilePath, profile)
    var compartment = CompartmentResource(id=compartmentId)
    var availabilityDomains:Set<AvailabilityDomain> = mutableSetOf()

    companion object { val DEFAULT_REGION = Region.US_PHOENIX_1 }


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


    fun instance(fn: InstanceResource.() -> Unit): InstanceResource {
        val v = InstanceResource(provider, region)
        v.compartment = compartment
        v.apply(fn)
        return v
    }

    fun internetGateway(fn: InternetGatewayResource.() -> Unit): InternetGatewayResource {
        val client = VirtualNetworkClient(provider)
        client.setRegion(region)
        val v = InternetGatewayResource(client)
        v.compartment = compartment
        v.apply(fn)
        return v
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



