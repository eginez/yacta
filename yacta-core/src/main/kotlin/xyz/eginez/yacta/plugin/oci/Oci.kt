package xyz.eginez.yacta.plugin.oci

import com.oracle.bmc.Region
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.identity.model.AvailabilityDomain
import xyz.eginez.yacta.core.Resource
import xyz.eginez.yacta.core.ResourceProperty
import xyz.eginez.yacta.core.logger
import java.nio.file.Paths

@DslMarker
annotation class ResourceMarker


abstract class OciBaseResource(
        @ResourceProperty var parentCompartment: CompartmentResource?,
        var region: Region) : Resource {

    protected val children = mutableListOf<Resource>()
    val LOG by logger()

    fun addChild(r: Resource) = children.add(r)
    override fun children(): List<Resource> =  children

}

var ociRef: Oci? = null


@ResourceMarker
class Oci(val region: Region,
          val compartmentId: String,
          val configFilePath: String = Paths.get("~/.oci", "config").toString(),
          profile: String = "DEFAULT") {

    var provider = ConfigFileAuthenticationDetailsProvider(configFilePath, profile)
    var availabilityDomains: Set<AvailabilityDomain> = mutableSetOf()
    var tenancy: CompartmentResource? = null

    init {
        ociRef = this
        tenancy = CompartmentResource(null, region)
        tenancy?.id = compartmentId
        availabilityDomains  =  AvailabilityDomains(this.provider, region, tenancy!!).get()
    }
}

