package com.eginez.yacta.resources.oci

import com.eginez.yacta.resources.DataProvider
import com.eginez.yacta.resources.Resource
import com.oracle.bmc.Region
import com.oracle.bmc.auth.AuthenticationDetailsProvider
import com.oracle.bmc.identity.IdentityClient
import com.oracle.bmc.identity.model.AvailabilityDomain
import com.oracle.bmc.identity.requests.ListAvailabilityDomainsRequest

class AvailabilityDomains(val configuration: AuthenticationDetailsProvider, val region: Region): DataProvider<Set<AvailabilityDomain>> {
    lateinit var compartment: CompartmentResource

    override fun get(): Set<AvailabilityDomain> {
        val client = IdentityClient(configuration)
        client.setRegion(region)
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


class CompartmentResource: Resource {
    var id: String = ""

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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dependencies(): List<Resource> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun id(): String {
        return id
    }
}


@ResourceMarker
fun compartment(fn: CompartmentResource.() -> Unit): CompartmentResource{
    val c = com.eginez.yacta.resources.oci.CompartmentResource()
    c.apply(fn)
    return c
}
