package xyz.eginez.yacta.plugin.oci

import com.oracle.bmc.Region
import com.oracle.bmc.auth.AuthenticationDetailsProvider
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.core.VirtualNetworkClient
import com.oracle.bmc.core.model.Image
import com.oracle.bmc.core.model.Shape
import com.oracle.bmc.identity.model.AvailabilityDomain
import com.oracle.bmc.objectstorage.ObjectStorageClient
import xyz.eginez.yacta.data.Resource
import xyz.eginez.yacta.data.logger
import java.nio.file.Paths

@DslMarker
annotation class ResourceMarker


val executionGraph: MutableList<Resource<*>> = mutableListOf()


interface Provisioner<T> {
    fun doCreate(resource: Resource<T>)
    fun doDestroy(resource: Resource<T>)
    fun doUpdate(resource: Resource<T>)
    fun doGet(resource: Resource<T>): T
}

abstract class OciBaseResource<T>(
        var compartment: CompartmentResource?,
        var region: Region,
        var provisioner: Provisioner<T>) : Resource<T> {

    val LOG by logger()

    //TODO fix dispatch of listeners
    var listeners: MutableSet<ResourceStateChangeListener> = mutableSetOf(LoggerListener(this))

    override fun create() {
        listeners.forEach { it.willCreate() }
        provisioner.doCreate(this)
        listeners.forEach { it.didCreate() }
    }

    override fun destroy() {
        listeners.forEach { it.willDestroy() }
        provisioner.doDestroy(this)
        listeners.forEach { it.didDestroy() }
    }

    override fun update() {
        listeners.forEach { it.willUpdate() }
        provisioner.doUpdate(this )
        listeners.forEach { it.didUpdate() }
    }

    override fun get(): T {
        return provisioner.doGet(this)
    }
}


interface ResourceStateChangeListener {
    fun willCreate()
    fun didCreate()
    fun willDestroy()
    fun didDestroy()
    fun willUpdate()
    fun didUpdate()
}

class LoggerListener(private val resource: OciBaseResource<*>) : ResourceStateChangeListener {
    override fun willCreate() = resource.LOG.info("will create: [$resource]")
    override fun didCreate() = resource.LOG.info("did create: [$resource]")
    override fun willDestroy() = resource.LOG.info("will destroy: [$resource]")
    override fun didDestroy() = resource.LOG.info("did destroy: [$resource]")
    override fun willUpdate() = resource.LOG.info("will update: [$resource]")
    override fun didUpdate() = resource.LOG.info("did update: [$resource]")
}


@ResourceMarker
class Oci(val region: Region,
          val compartmentId: String,
          val configFilePath: String = Paths.get("~/.oci", "config").toString(),
          profile: String = "DEFAULT") {

    var provider = ConfigFileAuthenticationDetailsProvider(configFilePath, profile)
    var compartment = CompartmentResource(id = compartmentId)
    var availabilityDomains: Set<AvailabilityDomain> = mutableSetOf()


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
        val v = InstanceResource(provider, region, compartment)
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

    static

}



