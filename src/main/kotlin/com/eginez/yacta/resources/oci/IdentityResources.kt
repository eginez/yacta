package com.eginez.yacta.resources.oci

import com.eginez.yacta.resources.DataProvider
import com.eginez.yacta.resources.Resource
import com.oracle.bmc.Region
import com.oracle.bmc.auth.AuthenticationDetailsProvider
import com.oracle.bmc.identity.IdentityClient
import com.oracle.bmc.identity.model.AvailabilityDomain
import com.oracle.bmc.identity.requests.ListAvailabilityDomainsRequest

class AvailabilityDomains(configuration: AuthenticationDetailsProvider): DataProvider<Set<AvailabilityDomain>> {
    lateinit var compartment: Compartment
    private val client = IdentityClient(configuration)

    override fun get(): Set<AvailabilityDomain> {
        client.setRegion(Region.US_PHOENIX_1)
        val availabilityDomainsRequest = ListAvailabilityDomainsRequest.builder()
                .compartmentId(compartment.id)
                .build()
        val availabilityDomainsResponse = client.listAvailabilityDomains(availabilityDomainsRequest)
        return availabilityDomainsResponse.items.toSet()
    }
}

class Compartment: Resource {
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
fun compartment(fn: Compartment.() -> Unit): Compartment{
    val c = com.eginez.yacta.resources.oci.Compartment()
    c.apply(fn)
    return c
}
