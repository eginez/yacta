package com.eginez.yacta.resources.oci

import com.eginez.yacta.resources.DataProvider
import com.eginez.yacta.resources.Resource
import com.oracle.bmc.Region
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.auth.AuthenticationDetailsProvider
import com.oracle.bmc.core.ComputeClient
import com.oracle.bmc.core.VirtualNetworkClient
import com.oracle.bmc.core.model.Image
import com.oracle.bmc.core.model.LaunchInstanceDetails
import com.oracle.bmc.core.model.Shape
import com.oracle.bmc.core.requests.LaunchInstanceRequest
import com.oracle.bmc.core.requests.ListImagesRequest
import com.oracle.bmc.core.requests.ListShapesRequest
import com.oracle.bmc.identity.model.AvailabilityDomain

class InstanceResource (private val provider: ConfigFileAuthenticationDetailsProvider, val region: Region?): Resource {

    lateinit var availabilityDomain: AvailabilityDomain
    lateinit var compartment: Compartment
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
    }

    override fun destroy() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun id(): String {
        return id.orEmpty()
    }

    override fun dependencies(): List<Resource> {
        return listOf(vnic as Resource)
    }

    override fun get(): Resource {
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

}

class ComputeImages(configuration: AuthenticationDetailsProvider ): DataProvider<Set<Image>> {
    var region = Region.US_PHOENIX_1
    private val client = ComputeClient(configuration)
    lateinit var compartment: Compartment

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

class ComputeShapes(configuration: AuthenticationDetailsProvider): DataProvider<Set<Shape>> {
    lateinit var compartment: Compartment
    var region = Region.US_PHOENIX_1
    private val client = ComputeClient(configuration)

    override fun get(): Set<Shape> {
        client.setRegion(region)
        val shapes = fullyList<Shape, ListShapesRequest>({page ->
            ListShapesRequest.builder()
                    .compartmentId(compartment.id)
                    .page(page)
                    .build()
        }, {r: ListShapesRequest ->
            val res = client.listShapes(r)
            Pair(res.opcNextPage, res.items)
        })
        return shapes.toSet()
    }
}
