package com.eginez.yacta.resources.oci

import com.eginez.yacta.resources.Resource
import com.oracle.bmc.core.VirtualNetworkClient
import com.oracle.bmc.core.model.*
import com.oracle.bmc.core.requests.*
import com.oracle.bmc.core.responses.CreateInternetGatewayResponse
import com.oracle.bmc.identity.model.AvailabilityDomain

class  VcnResource (val client: VirtualNetworkClient): Resource {

    var displayName: String = ""
    lateinit var compartment: Compartment
    var cidrBlock: String = ""
    var dnsLabel: String? = null
    private var id: String? = null

    override fun create() {

        var details = CreateVcnDetails.builder()
                .cidrBlock(cidrBlock)
                .compartmentId(compartment.id)
                .displayName(displayName)

        if (!dnsLabel.isNullOrBlank()) {
            details = details.dnsLabel(dnsLabel)
        }

        val request = CreateVcnRequest.builder()
                .createVcnDetails(details.build()).build()

        id = client.createVcn(request).vcn.id
        val waiter = client.waiters.forVcn(GetVcnRequest.builder().vcnId(id).build(), Vcn.LifecycleState.Available)
        waiter.execute()
        println("Created: " + this)
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

    override fun get(): Resource {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toString(): String {
        return "VcnResource(displayName='$displayName', compartmentId='${compartment.id}', cidrBlock='$cidrBlock', dnsLabel=$dnsLabel, id=$id)"
    }
}

class  SubnetResource (val client: VirtualNetworkClient): Resource {

    var vcn: VcnResource = VcnResource(client)
    lateinit var availabilityDomain: AvailabilityDomain
    var cidrBlock: String = ""
    lateinit var compartment: Compartment
    var name: String? = null
    var vcnId: String = ""
    var prohibitPubicIp: Boolean = false
    var routeTableId: String = ""
    var securityListIds: List<String> = emptyList()
    var dnsLabel: String = ""
    var id: String = ""

    override fun create() {
        dependencies().forEach { it.create() }
        this.vcnId = vcn.id()

        val d = toCreateSubnetDetails()
        val req = CreateSubnetRequest.builder().createSubnetDetails(d).build()
        val res = client.createSubnet(req)
        id = res.subnet.id
        println("Created: " + this)
    }

    private fun toCreateSubnetDetails(): CreateSubnetDetails? {
        var builder = CreateSubnetDetails.builder()
                .availabilityDomain(availabilityDomain.name)
                .cidrBlock(cidrBlock)
                .compartmentId(compartment.id)
                .vcnId(vcnId)
        name?.let {builder.displayName(it)}

        return builder.build()
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

    override fun get(): Resource {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class  VnicResource (val client: VirtualNetworkClient): Resource {

    var subnetId: String = ""
    var publicIp: Boolean = false
    var name: String? = null
    var hostname: String? = null
    var subnet: SubnetResource? = null

    override fun create() {
        //create dependencies
        dependencies().forEach { it.create() }

        subnetId = subnet?.id.orEmpty()
        println("Created: " + this)
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
        val builder = CreateVnicDetails.builder()
                .subnetId(subnetId)
                .assignPublicIp(publicIp)

        hostname?.let { builder.hostnameLabel(it) }
        name?.let {builder.displayName(it)}
        return builder.build()
    }

    fun subnet(fn: SubnetResource.() -> Unit): SubnetResource {
        val v = SubnetResource(client)
        v.apply(fn)
        subnet = v
        return v
    }

    override fun get(): Resource {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class InternetGatewayResource(val client: VirtualNetworkClient): Resource {
    val LOG by com.eginez.yacta.resources.logger()
    var internetGateway: InternetGateway? = null
    var displayName: String? = null
    var enabled: Boolean? = null
    lateinit var compartment: Compartment
    lateinit var vcn: VcnResource

    override fun id(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun create() {
        val builder = CreateInternetGatewayDetails.builder()
        builder.compartmentId(compartment.id())
                .vcnId(vcn.id())
        displayName?.let {builder.displayName(it)}
        enabled?.let { builder.isEnabled(enabled)}
        val request = CreateInternetGatewayRequest.builder()
                .createInternetGatewayDetails(builder.build())
                .build()

        internetGateway = client.createInternetGateway(request).internetGateway
        LOG.info("Created: ${this}")
    }

    override fun destroy() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun get(): Resource {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dependencies(): List<Resource> {
        return emptyList()
    }

    override fun toString(): String {
        return "InternetGatewayResource(internetGateway=$internetGateway, displayName=$displayName, enabled=$enabled, compartment=$compartment, vcn=$vcn)"
    }

}

class RouteTable(val client: VirtualNetworkClient): Resource {
    val LOG by com.eginez.yacta.resources.logger()

    override fun id(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun create() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun destroy() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun get(): Resource {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update() {
    }

    override fun dependencies(): List<Resource> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

data class RouteRule (
    val cidrBlock: String,
    val internetGateway: InternetGateway
)
