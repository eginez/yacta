package xyz.eginez.yacta.plugin.oci

import com.oracle.bmc.Region
import com.oracle.bmc.core.model.Vcn
import com.oracle.bmc.identity.model.Compartment
import org.junit.Test
import xyz.eginez.yacta.plugin.oci.*
import java.io.StringWriter


class ProvisionersTest {
    val oci = Oci(region = Region.US_PHOENIX_1,
            compartmentId = "ocidv1:tenancy:oc1:phx:1460406592660:aaaaaaaab4faofrfkxecohhjuivjq262pu")
    val writer = StringWriter()

    @Test
    fun compartmentTest() {
        val c = oci.compartment{
            name = "right here"
            description = "some descroption here"
            compartment {
                name =  "name 2"
                description = "description 2"
            }
        }

        val p = createTerraformProvisionerFor<CompartmentResource, Compartment>(writer, CompartmentResource::class)
        p?.doCreate(c)

        System.out.println(writer.toString())
    }

    @Test
    fun vcnTest() {
        val c = oci.vcn{
            displayName = "right here"
            cidrBlock = "some descroption here"
        }

        val p = createTerraformProvisionerFor<VcnResource, Vcn>(writer, VcnResource::class)
        p?.doCreate(c)

        System.out.println(writer.toString())
    }
}