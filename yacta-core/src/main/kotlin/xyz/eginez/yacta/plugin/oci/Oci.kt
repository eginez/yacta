package xyz.eginez.yacta.plugin.oci

import com.oracle.bmc.Region
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.identity.model.AvailabilityDomain
import xyz.eginez.yacta.data.Resource
import xyz.eginez.yacta.data.ResourceProperty
import xyz.eginez.yacta.data.logger
import xyz.eginez.yacta.plugin.oci.identity.AvailabilityDomains
import xyz.eginez.yacta.plugin.oci.identity.CompartmentResource
import java.nio.file.Paths

@DslMarker
annotation class ResourceMarker


abstract class OciBaseResource(
        @ResourceProperty var compartment: CompartmentResource?,
        var region: Region) : Resource {

    val LOG by logger()

}

var ociRef: Oci? = null


@ResourceMarker
class Oci(val region: Region,
          val compartmentId: String,
          val configFilePath: String = Paths.get("~/.oci", "config").toString(),
          profile: String = "DEFAULT") {

    var provider = ConfigFileAuthenticationDetailsProvider(configFilePath, profile)
    var availabilityDomains: Set<AvailabilityDomain> = mutableSetOf()
    var compartmentResource: CompartmentResource? = null

    init {
        ociRef = this
        compartmentResource = CompartmentResource(null, region)
        compartmentResource?.id = compartmentId
        availabilityDomains  =  AvailabilityDomains(this.provider, region, compartmentResource!!).get()
    }
}

