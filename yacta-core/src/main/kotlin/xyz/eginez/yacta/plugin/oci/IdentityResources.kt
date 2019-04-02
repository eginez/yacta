package xyz.eginez.yacta.plugin.oci

import com.oracle.bmc.Region
import com.oracle.bmc.auth.AuthenticationDetailsProvider
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.core.VirtualNetworkClient
import com.oracle.bmc.core.model.Vcn
import com.oracle.bmc.core.requests.GetVcnRequest
import com.oracle.bmc.core.requests.ListVcnsRequest
import com.oracle.bmc.identity.IdentityClient
import com.oracle.bmc.identity.model.AvailabilityDomain
import com.oracle.bmc.identity.model.Compartment
import com.oracle.bmc.identity.requests.GetCompartmentRequest
import com.oracle.bmc.identity.requests.ListAvailabilityDomainsRequest
import xyz.eginez.yacta.core.*
import java.util.*
import kotlin.reflect.KClass

class AvailabilityDomains(configurationProvider: AuthenticationDetailsProvider,
                          region: Region,
                          val compartment: CompartmentResource) : DataProvider<Set<AvailabilityDomain>> {
    private val client = createClient<IdentityClient>(configurationProvider, region, IdentityClient.builder())

    override fun get(): Set<AvailabilityDomain> {
        val items = fullyList<AvailabilityDomain, ListAvailabilityDomainsRequest>({ page ->
            ListAvailabilityDomainsRequest.builder()
                    .compartmentId(compartment.id())
                    .build()
        }, { r: ListAvailabilityDomainsRequest ->
            val response = client.listAvailabilityDomains(r)
            Pair(response.opcNextPage, response.items)

        })
        return items.toSet()
    }
}



@YactaResource
class CompartmentResource(
        parentCompartment: CompartmentResource?,
        region: Region) : OciBaseResource(parentCompartment, region) {

    @ResourceId
    var id: String? = null
    @ResourceProperty
    var name: String? = null
    @ResourceProperty
    var description: String? = null
    @ResourceProperty
    var freeformTags: Map<String, String>? = null
    @ResourceProperty
    var definedTags: Map<String, Map<String, Any>>? = null

    //Read only
    private var lifecycleState: Compartment.LifecycleState? = null
    private var timeCreated: Date? = null
    private var inactiveStatus: Long? = null


    override fun id(): String {
        return id.orEmpty()
    }

    override fun currentState(): ResourceState {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun compartment (fn: CompartmentResource.() -> Unit = {}): CompartmentResource {
        val c = CompartmentResource(this, region)
        c.apply(fn)
        addChild(c)
        return c
    }

    override fun refresh() {
        val configurationProvider = ConfigFileAuthenticationDetailsProvider("DEFAULT")
        val client = createClient<IdentityClient>(configurationProvider, region, IdentityClient.builder())
        val req = GetCompartmentRequest.builder()
                .compartmentId(id())
                .build()
        val c = client.getCompartment(req).compartment
        id = c.id
        name = c.name
    }

    override fun children(): List<Resource> {
        val configurationProvider = ociRef?.provider as AuthenticationDetailsProvider
        val client = createClient<VirtualNetworkClient>(configurationProvider, region, VirtualNetworkClient.builder())
        val request = ListVcnsRequest.builder()
                .compartmentId(id())
                .build()

        client.listVcns(request).items.forEach{ vcn ->
            val cp = VcnResource(parentCompartment, region)
         //   cp.displayName = vcn.displayName
        //    cp.cidrBlock = vcn.cidrBlock
            cp.id = vcn.id
            children.add(cp)
        }
        return children
    }
}

fun Oci.compartment(region: Region = this.region,
                    compartment: CompartmentResource? = this.tenancy,
                    fn: CompartmentResource.() -> Unit = {}): CompartmentResource {

    val v = CompartmentResource(compartment, region)
    v.apply(fn)
    compartment?.addChild(v)
    return v
}
