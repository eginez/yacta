package xyz.eginez.yacta.plugin.oci

import com.oracle.bmc.Region
import com.oracle.bmc.auth.AuthenticationDetailsProvider
import com.oracle.bmc.core.Compute
import com.oracle.bmc.core.ComputeClient
import com.oracle.bmc.core.ComputePaginators
import com.oracle.bmc.core.VirtualNetworkClient
import com.oracle.bmc.core.model.Image
import com.oracle.bmc.core.model.Instance
import com.oracle.bmc.core.model.LaunchInstanceDetails
import com.oracle.bmc.core.model.Shape
import com.oracle.bmc.core.requests.*
import com.oracle.bmc.core.responses.ListImagesResponse
import com.oracle.bmc.core.responses.ListShapesResponse
import com.oracle.bmc.identity.model.AvailabilityDomain
import xyz.eginez.yacta.data.DataProvider
import xyz.eginez.yacta.data.Resource

class InstanceResource(
        region: Region,
        compartment: CompartmentResource?,
        provisioner: Provisioner<Instance>) : OciBaseResource<Instance>(compartment, region, provisioner) {
    lateinit var availabilityDomain: AvailabilityDomain
    lateinit var image: Image
    lateinit var shape: Shape
    var vnic: VnicResource? = null
    var displayName: String? = null
    var hostLabel: String? = null
    var ipxeScript: String? = null
    var metadata = mutableMapOf<String, String>()
    var extendedMetadata: MutableMap<String, Any>? = null
    var sshPublicKey: String? = null

    private var id: String? = null
    private val client = createClient<ComputeClient>(configurationProvider, region, ComputeClient.builder()) as Compute



    override fun doCreate() {
        client.setRegion(region)
        val builder = LaunchInstanceDetails.builder()
        builder.availabilityDomain(availabilityDomain.name)
        builder.compartmentId(compartment?.id)
        builder.imageId(image.id)
        builder.shape(shape.shape)
        displayName?.let { builder.displayName(it) }
        hostLabel?.let { builder.hostnameLabel(hostLabel) }
        ipxeScript?.let { builder.ipxeScript(it) }
        sshPublicKey?.let { metadata["ssh_authorized_keys"] = it }
        builder.metadata(metadata)
        extendedMetadata?.let { builder.extendedMetadata(it) }


        dependencies().forEach { it.create() }

        vnic?.let { builder.createVnicDetails(it.toVnicDetails()) }


        val req = LaunchInstanceRequest.builder()
                .launchInstanceDetails(builder.build())
                .build()
        val resp = client.launchInstance(req)
        id = resp.instance.id

        LOG.info("Creating instance: $this")
        val instanceResponse = client.waiters.forInstance(GetInstanceRequest.builder()
                .instanceId(id).build(), Instance.LifecycleState.Running)
                .execute()
        id = instanceResponse.instance.id

        LOG.info("""Instance created: ${publicIp()}""")
    }

    fun publicIp(): String {
        val listVnicAttachments = client.listVnicAttachments(ListVnicAttachmentsRequest.builder()
                .compartmentId(compartment?.id())
                .instanceId(id)
                .build())

        if (listVnicAttachments.items.size == 0) {
            return ""
        }

        val first = listVnicAttachments.items.first()
        val vcnClient = VirtualNetworkClient(configurationProvider)
        vcnClient.setRegion(region)
        val vnicRes = vcnClient.getVnic(GetVnicRequest.builder()
                .vnicId(first.vnicId)
                .build())
        return vnicRes.vnic.publicIp
    }


    override fun doDestroy() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun id(): String {
        return id.orEmpty()
    }

    override fun dependencies(): List<Resource<*>> {
        return listOf(vnic as Resource<VnicResource>)
    }

    override fun get(): Instance {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun doUpdate() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun toString(): String {
        return "InstanceResource(region=$region, availabilityDomain=$availabilityDomain, compartment=$compartment, image=$image, shape=$shape, vnic=$vnic, displayName=$displayName, hostLabel=$hostLabel, ipxeScript=$ipxeScript, metadata=$metadata, extendedMetadata=$extendedMetadata, sshPublicKey=$sshPublicKey, id=$id, client=$client)"
    }

}

class ComputeImages(configurationProvider: AuthenticationDetailsProvider, region: Region) : DataProvider<Set<Image>> {
    private val client = createClient<ComputeClient>(configurationProvider, region, ComputeClient.builder())
    private val paginator = ComputePaginators(client)::listImagesResponseIterator
    lateinit var compartment: CompartmentResource

    override fun get(): Set<Image> {
        val req = ListImagesRequest.builder()
                .compartmentId(compartment.id)
                .build()
        val elements = fullyPaginate(req, paginator, ListImagesResponse::getItems)
        return elements
    }
}

fun InstanceResource.image(osName: String, osVersion: String = "", gpu: Boolean = false,
                           configurationProvider: AuthenticationDetailsProvider = this.configuration.provider,
                           region: Region = this.region,
                           compartment: CompartmentResource? = this.compartment): Image {
    val imgProvider = ComputeImages(configurationProvider, region)
    imgProvider.compartment = compartment!!
    val nameVersionFilter = { image: Image -> image.operatingSystem.contains(osName) && image.operatingSystemVersion.contains(osVersion) }
    val filter = if (gpu) { image: Image -> nameVersionFilter(image) && image.displayName.contains("GPU") }
    else { image: Image -> nameVersionFilter(image) && !image.displayName.contains("GPU") }

    try {
        return imgProvider.get().first(filter)
    } catch (e: NoSuchElementException) {
        throw NoSuchElementException("Can not find os: $osName with version $osVersion")
    }
}

class ComputeShapes(configurationProvider: AuthenticationDetailsProvider, region: Region) : DataProvider<Set<Shape>> {
    lateinit var compartment: CompartmentResource
    private val client = createClient<ComputeClient>(configurationProvider, region, ComputeClient.builder())
    private val paginator = ComputePaginators(client)::listShapesResponseIterator

    override fun get(): Set<Shape> {
        val request = ListShapesRequest.builder()
                .compartmentId(compartment.id)
                .build()
        val elements = fullyPaginate(request, paginator, ListShapesResponse::getItems)
        return elements
    }
}

fun InstanceResource.shape(name: String, vm: Boolean = true,
                           configurationProvider: AuthenticationDetailsProvider = this.configurationProvider,
                           region: Region = this.region,
                           compartment: CompartmentResource? = this.compartment): Shape {
    val shapeProvider = ComputeShapes(configurationProvider, region)
    shapeProvider.compartment = compartment!!
    val nameFilter = { s: Shape -> s.shape.contains(name) }
    val filter = if (vm) { s: Shape -> nameFilter(s) && s.shape.startsWith("VM") }
    else { s: Shape -> nameFilter(s) && !s.shape.startsWith("VM") }
    try {
        return shapeProvider.get().first(filter)
    } catch (e: NoSuchElementException) {
        throw NoSuchElementException("Can not find shape with name: $name for ${if (vm) "VM" else "no-VM"}")
    }
}
