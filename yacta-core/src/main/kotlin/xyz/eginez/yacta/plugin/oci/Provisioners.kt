package xyz.eginez.yacta.plugin.oci

import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.core.VirtualNetworkClient
import com.oracle.bmc.core.model.Vcn
import com.oracle.bmc.core.requests.GetVcnRequest
import com.oracle.bmc.identity.IdentityClient
import com.oracle.bmc.identity.model.Compartment
import com.oracle.bmc.identity.requests.GetCompartmentRequest
import org.jtwig.JtwigModel
import org.jtwig.JtwigTemplate
import org.jtwig.environment.DefaultEnvironmentConfiguration
import org.jtwig.environment.EnvironmentFactory
import org.jtwig.resource.reference.ResourceReference
import xyz.eginez.yacta.core.Provisioner
import xyz.eginez.yacta.core.Resource
import xyz.eginez.yacta.core.ResourceProperty
import java.io.Writer
import kotlin.reflect.KClass
import kotlin.reflect.full.*


data class TFResource(val name: String, val properties: Map<String,String>)

fun toTFRes(name: String, r: Any): TFResource {
    val tfProperties = mutableMapOf<String,String>()
    val klass = r.javaClass.kotlin
    klass.memberProperties.filter {
        it.findAnnotation<ResourceProperty>() != null &&
        it.getter.call(r) != null
    }.forEach { resourceProp ->
        val propertyValue = resourceProp.get(r)
        propertyValue?.let {
            val isRes = resourceProp
                    .returnType
                    .withNullability(false)
                    .isSubtypeOf(CompartmentResource::class.createType())
            var pVal = it.toString()
            if (isRes) {
                val parentCompartment = it as CompartmentResource
                pVal = parentCompartment.id()
            }
            tfProperties.put(resourceProp.name, pVal)
        }
    }
    return TFResource(name, tfProperties)
}



interface TerraformProvisioner

class DefaultTerraformProvisioner<T> (val writer: Writer,
                                      val terraformResourceName: String,
                                      val terraformResourceTemplate: String,
                                      val propertyNamesOverrides: Map<String, String>? = null,
                                      val getter: (Resource) -> T = { TODO("not implemented") }): Provisioner<T>, TerraformProvisioner {

    override fun doCreate(resource: Resource) {
        val tfRes = provision(terraformResourceName,  resource, terraformResourceTemplate)
        writer.write(tfRes)
    }

    override fun doDestroy(resource: Resource) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun doUpdate(resource: Resource) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun doGet(resource: Resource): T {
        return getter(resource)
    }

    fun provision(name: String, r: Any, template: String): String {
        val resource = toTFRes(name, r)

        val config = DefaultEnvironmentConfiguration()
        val env = EnvironmentFactory().create(config)

        val jTemplate = JtwigTemplate(env, ResourceReference(ResourceReference.STRING, template))
        val model = JtwigModel.newModel().with("resource", resource)
        return jTemplate.render(model)
    }


}


fun <T: Resource, M> createTerraformProvisionerFor(writer: Writer, klass: KClass<T>): Provisioner<M>? {

    when {
        klass == CompartmentResource::class -> {
            val compartmentProvisioner = DefaultTerraformProvisioner<M>(
                    terraformResourceName = "oci_identity_compartment",
                    terraformResourceTemplate = """
resource "{{ resource.name }}" "create_name" { {% for k,val in resource.properties  %}
    "{{ k }}" = "{{ val }}" {% endfor %}
}
""",
                    writer = writer)
            return compartmentProvisioner
        }
        klass == VcnResource::class -> {
            val vcnProvisioner = DefaultTerraformProvisioner<M> (
                    terraformResourceName = "oci_network_vcn",
                    terraformResourceTemplate = """
resource "{{ resource.name }}" "create_name" { {% for k,val in resource.properties  %}
    "{{ k }}" = "{{ val }}" {% endfor %}
}
""",
                    writer=writer)
            return vcnProvisioner
        }
    }
    return null
}

