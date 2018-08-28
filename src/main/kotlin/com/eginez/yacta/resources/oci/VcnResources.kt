package com.eginez.yacta.resources.oci

import com.eginez.yacta.resources.Resource
import com.oracle.bmc.auth.AuthenticationDetailsProvider
import com.oracle.bmc.core.VirtualNetworkClient
import com.oracle.bmc.core.model.*
import com.oracle.bmc.core.requests.*
import com.oracle.bmc.identity.model.AvailabilityDomain
import javax.print.attribute.standard.Destination

class  VcnResource (val client: VirtualNetworkClient): Resource<Vcn> {

    val LOG by com.eginez.yacta.resources.logger()
    var displayName: String = ""
    lateinit var compartment: CompartmentResource
    var cidrBlock: String = ""
    var dnsLabel: String? = null
    private var id: String? = null

    private var routeTableResource: RouteTableResource? = null

    @Synchronized override fun create() {

        if (id != null) {
            LOG.info("vcn has been already created: $this")
            return
        }
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
        val vcn = waiter.execute().vcn
        println("Created: " + this)

        //TODO move all this to execution graph
        routeTableResource?.let {
            it.id = vcn.defaultRouteTableId
            it.update()
        }
    }

    fun routeTable(fn: RouteTableResource.() -> Unit): RouteTableResource {
        routeTableResource = RouteTableResource(client)
        routeTableResource!!.apply(fn)
        return routeTableResource!!

        //This should add a task to the execution graph
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

    override fun dependencies(): List<Resource<*>> {
        return emptyList()
    }

    override fun get(): Vcn {
        TODO("not implemented")
    }

    override fun update() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toString(): String {
        return "VcnResource(displayName='$displayName', compartmentId='${compartment.id}', cidrBlock='$cidrBlock', dnsLabel=$dnsLabel, id=$id)"
    }

}

class  SubnetResource (val client: VirtualNetworkClient): Resource<Subnet> {

    var vcn: VcnResource = VcnResource(client)
    lateinit var availabilityDomain: AvailabilityDomain
    var cidrBlock: String = ""
    lateinit var compartment: CompartmentResource
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

    override fun dependencies(): List<Resource<*>> {
        return listOf(vcn as Resource<*>)
    }

    override fun get(): Subnet {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class  VnicResource (val client: VirtualNetworkClient): Resource<Vnic> {

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

    override fun dependencies(): List<Resource<*>> {
        return listOf(subnet as Resource<*>)
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

    override fun get(): Vnic {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class InternetGatewayResource(val client: VirtualNetworkClient): Resource<InternetGateway> {
    val LOG by com.eginez.yacta.resources.logger()
    var internetGateway: InternetGateway? = null
    var displayName: String? = null
    var enabled: Boolean? = null
    lateinit var compartment: CompartmentResource
    lateinit var vcn: VcnResource
    var id: String = ""

    override fun id(): String {
        return id
    }

    override fun create() {
        dependencies().forEach { it.create()}
        val builder = CreateInternetGatewayDetails.builder()
        builder.compartmentId(compartment.id())
                .vcnId(vcn.id())
        displayName?.let {builder.displayName(it)}
        enabled?.let { builder.isEnabled(enabled)}
        val request = CreateInternetGatewayRequest.builder()
                .createInternetGatewayDetails(builder.build())
                .build()

        internetGateway = client.createInternetGateway(request).internetGateway
        id = internetGateway?.id!!
        LOG.info("Created: ${this}")
    }

    override fun destroy() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun get(): InternetGateway {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dependencies(): List<Resource<*>> {
        return listOf(vcn as Resource<*>)
    }

    override fun toString(): String {
        return "InternetGatewayResource(internetGateway=$internetGateway, displayName=$displayName, enabled=$enabled, compartment=$compartment, vcn=$vcn)"
    }
}



class RouteTableResource(val client: VirtualNetworkClient): Resource<RouteTable> {
    val LOG by com.eginez.yacta.resources.logger()
    var rules: MutableList<RouteRuleResource> = mutableListOf()
    var id: String? = null
    var displayName: String? = null
    var vcn: VcnResource? = null


    override fun id(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun create() {
    }

    override fun destroy() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun get(): RouteTable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update() {
        val rlz = rules.map { it.create(); it.routeRule!! }
        val request = UpdateRouteTableRequest.builder()
                .updateRouteTableDetails(UpdateRouteTableDetails.builder()
                        .routeRules(rlz).build())
                .rtId(id)
                .build()
        client.updateRouteTable(request)
        LOG.info("Created $this")

    }

    override fun dependencies(): List<Resource<*>> {
        return listOf(vcn as Resource<*>)
    }

    fun rule(destination: String, gateway: InternetGatewayResource) {
        val rule = RouteRuleResource()
        rule.ig = gateway
        rule.destination = destination
        rule.destinationType = RouteRule.DestinationType.CidrBlock
        rules.add(rule)
    }

    override fun toString(): String {
        return "RouteTableResource(rules=$rules, id=$id, displayName=$displayName, vcn=$vcn)"
    }
}



class RouteRuleResource: Resource<RouteRule> {
    var cidrBlock: String = ""
    var destination: String = ""
    var destinationType: RouteRule.DestinationType? = null
    var networkEntityId: String? = null

    var ig: InternetGatewayResource? = null

    var routeRule: RouteRule? = null


    override fun id(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun create() {
        dependencies().forEach { it.create() }

        val builder = RouteRule.builder()
                .destination(destination)
                .networkEntityId(ig?.id)
        destinationType?.let { builder.destinationType(destinationType) }
        routeRule = builder.build()
    }

    override fun destroy() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun get(): RouteRule {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dependencies(): List<Resource<*>> {
        val deps = mutableListOf<Resource<*>>()
        ig?.let { deps.add(it) }
        return deps
    }

    override fun toString(): String {
        return super.toString()
    }

}

