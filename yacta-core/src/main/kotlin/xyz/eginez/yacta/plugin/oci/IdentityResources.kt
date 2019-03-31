package xyz.eginez.yacta.plugin.oci

import com.oracle.bmc.Region
import com.oracle.bmc.auth.AuthenticationDetailsProvider
import com.oracle.bmc.identity.IdentityClient
import com.oracle.bmc.identity.model.AvailabilityDomain
import com.oracle.bmc.identity.model.Compartment
import com.oracle.bmc.identity.requests.ListAvailabilityDomainsRequest
import xyz.eginez.yacta.core.*
import java.util.*

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

}

fun Oci.compartment(region: Region = this.region,
                    compartment: CompartmentResource? = this.tenancy,
                    fn: CompartmentResource.() -> Unit = {}): CompartmentResource {

    val v = CompartmentResource(compartment, region)
    v.apply(fn)
    compartment?.addChild(v)
    return v
}
