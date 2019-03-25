package xyz.eginez.yacta.plugin.oci.provisioners

import com.oracle.bmc.Region
import com.oracle.bmc.identity.model.Compartment
import org.junit.Test
import xyz.eginez.yacta.plugin.oci.Oci
import xyz.eginez.yacta.plugin.oci.identity.*
import java.io.StringWriter


class ProvisionersTest {
    val oci = Oci(region = Region.US_PHOENIX_1,
            compartmentId = "ocidv1:tenancy:oc1:phx:1460406592660:aaaaaaaab4faofrfkxecohhjuivjq262pu")

    @Test
    fun simpleTest() {
        val c = oci.compartment{
            name = "right here"
            description = "some descroption here"
            compartment {
                name =  "name 2"
                description = "description 2"
            }
        }

        val w = StringWriter()
        val p = createTerraformProvisionerFor<CompartmentResource, Compartment>(w, CompartmentResource::class)
        p?.doCreate(c)

        System.out.println(w.toString())
    }
}