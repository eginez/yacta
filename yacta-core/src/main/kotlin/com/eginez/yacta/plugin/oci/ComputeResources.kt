package com.eginez.yacta.plugin.oci

import com.eginez.yacta.data.DataProvider
import com.eginez.yacta.data.Resource
import com.eginez.yacta.data.logger
import com.oracle.bmc.Region
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.auth.AuthenticationDetailsProvider
import com.oracle.bmc.core.ComputeClient
import com.oracle.bmc.core.VirtualNetworkClient
import com.oracle.bmc.core.model.Image
import com.oracle.bmc.core.model.Instance
import com.oracle.bmc.core.model.LaunchInstanceDetails
import com.oracle.bmc.core.model.Shape
import com.oracle.bmc.core.requests.*
import com.oracle.bmc.identity.model.AvailabilityDomain

class InstanceResource (private val provider: ConfigFileAuthenticationDetailsProvider, val region: Region?): Resource<Instance> {

    val LOG by logger()
    lateinit var availabilityDomain: AvailabilityDomain
    lateinit var compartment: CompartmentResource
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
    private var client: ComputeClient = ComputeClient(provider)


    override fun create() {
        client.setRegion(region)
        val builder = LaunchInstanceDetails.builder()
        builder.availabilityDomain(availabilityDomain.name)
        builder.compartmentId(compartment.id)
        builder.imageId(image.id)
        builder.shape(shape.shape)
        displayName?.let { builder.displayName(it) }
        hostLabel?.let {builder.hostnameLabel(hostLabel)}
        ipxeScript?.let { builder.ipxeScript(it) }
        sshPublicKey?.let {  metadata["ssh_authorized_keys"] = it }
        builder.metadata(metadata)
        extendedMetadata?.let {builder.extendedMetadata(it)}


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
                .compartmentId(compartment.id())
                .instanceId(id)
                .build())

        if (listVnicAttachments.items.size == 0) {
            return ""
        }

        val first = listVnicAttachments.items.first()
        val vcnClient = VirtualNetworkClient(provider)
        vcnClient.setRegion(region)
        val vnicRes = vcnClient.getVnic(GetVnicRequest.builder()
                .vnicId(first.vnicId)
                .build())
        return vnicRes.vnic.publicIp
    }


    override fun destroy() {
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

    override fun update() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun vnic(fn: VnicResource.() -> Unit): VnicResource {
        val vcnClient = VirtualNetworkClient(provider)
        vcnClient.setRegion(region)
        val v = VnicResource(vcnClient)
        v.apply(fn)
        return v
    }

    override fun toString(): String {

        return "InstanceResource(provider=$provider, region=$region, availabilityDomain=$availabilityDomain, compartment=$compartment, image=$image, shape=$shape, vnic=$vnic, displayName=$displayName, hostLabel=$hostLabel, ipxeScript=$ipxeScript, metadata=$metadata, extendedMetadata=$extendedMetadata, sshPublicKey=$sshPublicKey, id=$id, client=$client)"
    }

}

class ComputeImages(configuration: AuthenticationDetailsProvider, private val region: Region ): DataProvider<Set<Image>> {
    private val client = ComputeClient(configuration)
    lateinit var compartment: CompartmentResource

    override fun get(): Set<Image> {
        client.setRegion(region)
        val images = fullyList<Image, ListImagesRequest>({ page ->
            ListImagesRequest.builder()
                    .compartmentId(compartment.id)
                    .page(page)
                    .build()
        }, { r: ListImagesRequest ->
            val response = client.listImages(r)
            Pair(response.opcNextPage, response.items)
        })
        return images.toSet()
    }

}

class ComputeShapes(configuration: AuthenticationDetailsProvider, private val region: Region): DataProvider<Set<Shape>> {
    lateinit var compartment: CompartmentResource
    private val client = ComputeClient(configuration)

    override fun get(): Set<Shape> {
        client.setRegion(region)
        val shapes = fullyList<Shape, ListShapesRequest>({ page ->
            ListShapesRequest.builder()
                    .compartmentId(compartment.id)
                    .page(page)
                    .build()
        }, { r: ListShapesRequest ->
            val res = client.listShapes(r)
            Pair(res.opcNextPage, res.items)
        })
        return shapes.toSet()
    }
}
