package com.eginez.yacta.resources

import com.oracle.bmc.core.VirtualNetworkClient
import com.oracle.bmc.core.model.CreateSubnetDetails
import com.oracle.bmc.core.model.CreateVcnDetails
import com.oracle.bmc.core.model.CreateVnicDetails
import com.oracle.bmc.core.model.Vcn
import com.oracle.bmc.core.requests.CreateSubnetRequest
import com.oracle.bmc.core.requests.CreateVcnRequest
import com.oracle.bmc.core.requests.DeleteVcnRequest
import com.oracle.bmc.core.requests.GetVcnRequest

class  VcnResource (val client: VirtualNetworkClient): Resource {

    var displayName: String = ""
    var compartmentId: String = ""
    var cidrBlock: String = ""
    var dnsLabel: String? = null
    private var id: String? = null

    override fun create() {

        var details = CreateVcnDetails.builder()
                .cidrBlock(cidrBlock)
                .compartmentId(compartmentId)
                .displayName(displayName)

        if (!dnsLabel.isNullOrBlank()) {
            details = details.dnsLabel(dnsLabel)
        }

        val request = CreateVcnRequest.builder()
                .createVcnDetails(details.build()).build()

        id = client.createVcn(request).vcn.id
        val waiter = client.waiters.forVcn(GetVcnRequest.builder().vcnId(id).build(), Vcn.LifecycleState.Available)
        waiter.execute()

    }

    override fun destroy() {
        if (id != null) {
            val req = DeleteVcnRequest.builder().vcnId(id).build()
            client.deleteVcn(req)
            val waiter = client.waiters.forVcn(GetVcnRequest.builder().vcnId(id).build(), Vcn.LifecycleState.Terminated)
            waiter.execute()
        }
    }

    override fun id(): String {
        return id.orEmpty()
    }

    override fun dependencies(): List<Resource> {
        return emptyList()
    }

    override fun toString(): String {
        return "VcnResource(displayName='$displayName', compartmentId='$compartmentId', cidrBlock='$cidrBlock', dnsLabel=$dnsLabel, id=$id)"
    }
}

class  SubnetResource (val client: VirtualNetworkClient): Resource {
    var vcn: VcnResource = VcnResource(client)
    var availabilityDomain: String = ""
    var cidrBlock: String = ""
    var compartment: String = ""
    var vcnId: String = ""
    var prohibitPubicIp: Boolean = false
    var routeTableId: String = ""
    var securityListIds: List<String> = emptyList()
    var dnsLabel: String = ""
    var id: String = ""

    override fun create() {
        vcn.create()
        this.vcnId = vcn.id()
        val d = toCreateSubnetDetails()
        var req = CreateSubnetRequest.builder().createSubnetDetails(d).build()
        var res = client.createSubnet(req)
        id = res.subnet.id
    }

    private fun toCreateSubnetDetails(): CreateSubnetDetails? {
        return CreateSubnetDetails.builder()
                .availabilityDomain(availabilityDomain)
                .cidrBlock(cidrBlock)
                .compartmentId(compartment)
                .vcnId(vcnId)
                .build()
    }

    override fun destroy() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun id(): String {
        return id
    }


    fun vcn(fn: VcnResource.() -> Unit): VcnResource {
        val v = VcnResource(client)
        v.apply(fn)
        vcn = v
        return v
    }

    override fun dependencies(): List<Resource> {
        return listOf(vcn as Resource)
    }
}

class  VnicResource (val client: VirtualNetworkClient): Resource {

    var subnetId: String = ""
    var publicIp: Boolean = false
    var name: String = ""
    var hostname: String = ""
    var subnet: SubnetResource? = null

    override fun create() {
        //create dependencies
        subnet?.create()
        subnetId = subnet?.id.orEmpty()
        //no op
        return
    }

    override fun destroy() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun id(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dependencies(): List<Resource> {
        return listOf(subnet as Resource)
    }

    fun toVnicDetails(): CreateVnicDetails {
        return CreateVnicDetails.builder()
                .subnetId(subnetId)
                .assignPublicIp(publicIp)
                .displayName(name)
                .hostnameLabel(hostname)
                .build()
    }

    fun subnet(fn: SubnetResource.() -> Unit): SubnetResource {
        val v = SubnetResource(client)
        v.apply(fn)
        subnet = v
        return v
    }
}
