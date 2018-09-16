package xyz.eginez.yacta.plugin.oci

import xyz.eginez.yacta.data.DataProvider
import xyz.eginez.yacta.data.Resource
import com.oracle.bmc.Region
import com.oracle.bmc.auth.AuthenticationDetailsProvider
import com.oracle.bmc.identity.IdentityClient
import com.oracle.bmc.identity.model.AvailabilityDomain
import com.oracle.bmc.identity.model.Compartment
import com.oracle.bmc.identity.requests.ListAvailabilityDomainsRequest

class AvailabilityDomains(configurationProvider: AuthenticationDetailsProvider,
                          val region: Region,
                          val compartment: CompartmentResource): DataProvider<Set<AvailabilityDomain>> {
    private val client = createClient<IdentityClient>(configurationProvider, region, IdentityClient.builder())

    override fun get(): Set<AvailabilityDomain> {
        val items = fullyList<AvailabilityDomain, ListAvailabilityDomainsRequest>({ page ->
            ListAvailabilityDomainsRequest.builder()
                    .compartmentId(compartment.id)
                    .build()
        }, { r: ListAvailabilityDomainsRequest ->
            val response = client.listAvailabilityDomains(r)
            Pair(response.opcNextPage, response.items)

        })
        return items.toSet()
    }
}

val <T> OciBaseResource<T>.availabilityDomains: Set<AvailabilityDomain>
    get() = AvailabilityDomains(configurationProvider, region, compartment!!).get()



class CompartmentResource(val id: String=""): Resource<Compartment> {
    override fun create() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun destroy() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun get(): Compartment {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dependencies(): List<Resource<*>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun id(): String {
        return id
    }
}


@ResourceMarker
fun compartment(fn: CompartmentResource.() -> Unit): CompartmentResource {
    val c = CompartmentResource()
    c.apply(fn)
    return c
}
