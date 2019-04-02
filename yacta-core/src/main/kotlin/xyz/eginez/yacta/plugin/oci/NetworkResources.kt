package xyz.eginez.yacta.plugin.oci

import com.oracle.bmc.Region
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.core.VirtualNetworkClient
import com.oracle.bmc.core.model.Vcn
import com.oracle.bmc.core.requests.GetVcnRequest
import xyz.eginez.yacta.core.*
import java.util.*

@YactaResource
class VcnResource(
        parentCompartment: CompartmentResource?,
        region: Region) : OciBaseResource(parentCompartment, region) {

    @ResourceId
    var id: String? = null
    @ResourceProperty
    var cidrBlock: String? = null
    @ResourceProperty
    var defaultDhcpOptionsId: String? = null
    @ResourceProperty
    var defaultRouteTableId: String? = null
    @ResourceProperty
    var defaultSecurityListId: String? = null
    @ResourceProperty
    var definedTags: Map<String, Map<String, Any>>? = null
    @ResourceProperty
    var displayName: String? = null
    @ResourceProperty
    var dnsLabel: String? = null
    @ResourceProperty
    var freeformTags: Map<String, String>? = null
    @ResourceProperty
    var vcnDomainName: String? = null

    //var routeTableResource: RouteTableResource? = null
    var lifecycleState: Vcn.LifecycleState? = null
    var timeCreated: Date? = null


    override fun id(): String {
        return id.orEmpty()
    }

    override fun currentState(): ResourceState {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun refresh() {
        val configurationProvider = ConfigFileAuthenticationDetailsProvider("DEFAULT")
        val client = createClient<VirtualNetworkClient>(configurationProvider, region, VirtualNetworkClient.builder())
        val req = GetVcnRequest.builder()
                .vcnId(id())
                .build()
        val c = client.getVcn(req).vcn
        id = c.id
        displayName = c.displayName
    }
}

fun Oci.vcn(region: Region = this.region,
            compartment: CompartmentResource? = this.tenancy,
            fn: VcnResource.() -> Unit = {}): VcnResource {

    val v = VcnResource(compartment, region)
    v.apply(fn)
    compartment?.addChild(v)
    return v
}

fun CompartmentResource.vcn(region: Region = this.region,
                            compartment: CompartmentResource? = this,
                            fn: VcnResource.() -> Unit = {}): VcnResource {

    val v = VcnResource(compartment, region)
    v.apply(fn)
    this.addChild(v)
    return v
}
/*

class DefaultVcnResourceProvisioner (configurationProvider: AuthenticationDetailsProvider ) : Provisioner<Vcn> {
    private val client = createClient<VirtualNetworkClient>(configurationProvider, VirtualNetworkClient.builder())

    @Synchronized
    override fun doCreate(res: Resource<Vcn>) {
        val resource = res as VcnResource
        if (resource.id != null) {
            return
        }

        client.setRegion(resource.region)

        var details = CreateVcnDetails.builder()
                .cidrBlock(resource.cidrBlock)
                .compartmentId(resource.parentCompartment?.id)
                .displayName(resource.displayName)

        if (!resource.dnsLabel.isNullOrBlank()) {
            details = details.dnsLabel(resource.dnsLabel)
        }

        val request = CreateVcnRequest.builder()
                .createVcnDetails(details.build()).build()

        resource.id = client.createVcn(request).vcn.id
        val waiter = client.waiters.forVcn(
                GetVcnRequest.builder()
                        .vcnId(resource.id)
                        .build(),
                Vcn.LifecycleState.Available)
        val vcn = waiter.execute().vcn

        //TODO move all this to execution graph
        resource.routeTableResource?.let {
            it.id = vcn.defaultRouteTableId
            it.update()
        }
    }

    override fun doDestroy(res: Resource<Vcn>) {
        val resource = res as VcnResource
        if (resource.id != null) {
            val req = DeleteVcnRequest.builder().vcnId(resource.id).build()
            client.deleteVcn(req)
            val waiter = client.waiters
                    .forVcn(GetVcnRequest.builder()
                            .vcnId(resource.id)
                            .build(), Vcn.LifecycleState.Terminated)
            waiter.execute()
        }
    }

    override fun doGet(res: Resource<Vcn>): Vcn {
        val resource = res as VcnResource
        val vcnRequest = GetVcnRequest.builder()
                .vcnId(resource.id)
                .build()

        return client.getVcn(vcnRequest).vcn
    }

    override fun doUpdate(resource: Resource<Vcn>) {
        TODO("not implemented")
    }
}




class DefaultSubnetResourceProvisioner (configurationProvider: AuthenticationDetailsProvider ) : Provisioner<Subnet> {
    private val client = createClient<VirtualNetworkClient>(configurationProvider, VirtualNetworkClient.builder())

    override fun doCreate(resource: Resource<Subnet>) {
        val subnet = resource as SubnetResource
        val d = toCreateSubnetDetails(subnet)
        val req = CreateSubnetRequest.builder().createSubnetDetails(d).build()
        val res = client.createSubnet(req)
        subnet.id = res.subnet.id
    }

    private fun toCreateSubnetDetails(subnet: SubnetResource): CreateSubnetDetails? {
        var builder = CreateSubnetDetails.builder()
                .availabilityDomain(subnet.availabilityDomain.name)
                .cidrBlock(subnet.cidrBlock)
                .compartmentId(subnet.parentCompartment.id)
                .vcnId(subnet.vcnId)
        subnet.name?.let { builder.displayName(it) }

        return builder.build()
    }

    override fun doDestroy(resource: Resource<Subnet>) {
        val subnet = resource as SubnetResource
        var deleteSubnetRequest = DeleteSubnetRequest.builder()
                .subnetId(subnet.id)
                .build()

        client.deleteSubnet(deleteSubnetRequest)
    }

    override fun doUpdate(resource: Resource<Subnet>) {
        val subnet = resource as SubnetResource
        var details = UpdateSubnetDetails.builder()
                .displayName(subnet.name)
                .build()

        var request = UpdateSubnetRequest.builder()
                .updateSubnetDetails(details)
                .build()
        client.updateSubnet(request)
    }

    override fun doGet(resource: Resource<Subnet>): Subnet {
        var request = GetSubnetRequest.builder()
                .subnetId(resource.id())
                .build()
        val response = client.getSubnet(request)
        return response.subnet
    }

}

class SubnetResource(val client: VirtualNetworkClient) : Resource<Subnet> {

    lateinit var vcn: VcnResource
    lateinit var availabilityDomain: AvailabilityDomain
    var cidrBlock: String = ""
    lateinit var parentCompartment: CompartmentResource
    var name: String? = null
    var vcnId: String = ""
    var prohibitPubicIp: Boolean = false
    var routeTableId: String = ""
    var securityListIds: List<String> = emptyList()
    var dnsLabel: String = ""
    var id: String = ""


    override fun destroy() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun id(): String {
        return id
    }


    /*
    fun vcn(fn: VcnResource.() -> Unit): VcnResource {
        val v = VcnResource()
        v.apply(fn)
        vcn = v
        return v
    }
    */

    override fun children(): List<Resource<*>> {
        return listOf(vcn as Resource<*>)
    }

    override fun get(): Subnet {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class VnicResource(
        parentCompartment: CompartmentResource?,
        region: Region,
        provisioner: Provisioner<Vnic>) : OciBaseResource<Vnic>(parentCompartment, region, provisioner) {
    var subnetId: String = ""
    var publicIp: Boolean = false
    var name: String? = null
    var hostname: String? = null
    var subnet: SubnetResource? = null

    override fun create() {
        //create children
        children().forEach { it.create() }
        provisioner.doCreate(this)
    }

    override fun destroy() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun id(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun children(): List<Resource<*>> {
        return listOf(subnet as Resource<*>)
    }

    fun toVnicDetails(): CreateVnicDetails {
        val builder = CreateVnicDetails.builder()
                .subnetId(subnetId)
                .assignPublicIp(publicIp)

        hostname?.let { builder.hostnameLabel(it) }
        name?.let { builder.displayName(it) }
        return builder.build()
    }

    fun subnet(fn: SubnetResource.() -> Unit): SubnetResource {
        val v = SubnetResource()
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

class DefaultVnicResourceProvisioner (configurationProvider: AuthenticationDetailsProvider ) : Provisioner<Vnic> {
    private val client = createClient<ComputeClient>(configurationProvider, ComputeClient.builder())

    override fun doCreate(resource: Resource<Vnic>) {
        val res  = resource as VnicResource
        res.subnetId = res.subnet?.id.orEmpty()
    }

    override fun doDestroy(resource: Resource<Vnic>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    override fun doUpdate(resource: Resource<Vnic>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun doGet(resource: Resource<Vnic>): Vnic {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

fun InstanceResource.vnic(region: Region = this.region,
                          parentCompartment: CompartmentResource? = this.parentCompartment,
                          customProvisioner: Provisioner<Vnic>? = null,
                          fn: VnicResource.() -> Unit = {}): VnicResource {

    val provisioner  = customProvisioner ?: DefaultVnicResourceProvisioner(ociRef?.provider as AuthenticationDetailsProvider)
    val v = VnicResource(parentCompartment, region, provisioner )
    v.apply(fn)
    return v
}


class InternetGatewayResource(val client: VirtualNetworkClient) : Resource<InternetGateway> {
    val LOG by logger()
    var internetGateway: InternetGateway? = null
    var displayName: String? = null
    var enabled: Boolean? = null
    lateinit var parentCompartment: CompartmentResource
    lateinit var vcn: VcnResource
    var id: String = ""

    override fun id(): String {
        return id
    }

    override fun create() {
        children().forEach { it.create() }
        val builder = CreateInternetGatewayDetails.builder()
        builder.compartmentId(parentCompartment.id())
                .vcnId(vcn.id())
        displayName?.let { builder.displayName(it) }
        enabled?.let { builder.isEnabled(enabled) }
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

    override fun children(): List<Resource<*>> {
        return listOf(vcn as Resource<*>)
    }

    override fun toString(): String {
        return "InternetGatewayResource(internetGateway=$internetGateway, displayName=$displayName, enabled=$enabled, tenancy=$parentCompartment, vcn=$vcn)"
    }
}


class RouteTableResource(val client: VirtualNetworkClient) : Resource<RouteTable> {
    val LOG by logger()
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

    override fun children(): List<Resource<*>> {
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


class RouteRuleResource : Resource<RouteRule> {
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
        children().forEach { it.create() }

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

    override fun children(): List<Resource<*>> {
        val deps = mutableListOf<Resource<*>>()
        ig?.let { deps.add(it) }
        return deps
    }

    override fun toString(): String {
        return super.toString()
    }

}

//This should add a task to the execution graph
fun VcnResource.routeTable(fn: RouteTableResource.() -> Unit): RouteTableResource {
    routeTableResource = RouteTableResource(client)
    routeTableResource!!.apply(fn)
    return routeTableResource!!

}
*/
