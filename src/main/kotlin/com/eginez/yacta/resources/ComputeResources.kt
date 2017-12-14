package com.eginez.yacta.resources

import com.oracle.bmc.Region
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.core.ComputeClient
import com.oracle.bmc.core.VirtualNetworkClient
import com.oracle.bmc.core.model.LaunchInstanceDetails
import com.oracle.bmc.core.requests.LaunchInstanceRequest

class InstanceResource (val provider: ConfigFileAuthenticationDetailsProvider, val region: Region?): Resource {
    var availabilityDomain: String = ""
    var compartment: String = ""
    var image: String = ""
    var shape: String =""
    var vnic: VnicResource? = null
    var displayName: String = ""
    var hostLabel: String = ""
    var ipxeScript: String = ""
    var metadata: Map<String, String> = emptyMap()
    var extendedMetadata: Map<String, String> = emptyMap()

    private var id: String? = null
    private var client: ComputeClient = ComputeClient(provider)


    override fun create() {
        client.setRegion(region)
        val builder = LaunchInstanceDetails.builder()
        builder.availabilityDomain(availabilityDomain)
        builder.compartmentId(compartment)
        builder.imageId(image)
        builder.shape(shape)
        builder.displayName(displayName)
        builder.hostnameLabel(hostLabel)
        builder.ipxeScript(ipxeScript)
        builder.metadata(metadata)
        builder.extendedMetadata(extendedMetadata)

        if (vnic != null){
            builder.createVnicDetails(vnic?.toVnicDetails())
        }

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

    fun vnic(fn: VnicResource.() -> Unit): VnicResource {
        val vcnClient = VirtualNetworkClient(provider)
        vcnClient.setRegion(region)
        val v = VnicResource(vcnClient)
        v.apply(fn)
        return v
    }

}