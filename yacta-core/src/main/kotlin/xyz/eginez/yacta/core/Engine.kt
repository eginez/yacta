package xyz.eginez.yacta.core

import com.oracle.bmc.core.model.Vcn
import com.oracle.bmc.identity.model.Compartment
import xyz.eginez.yacta.plugin.oci.CompartmentResource
import xyz.eginez.yacta.plugin.oci.VcnResource
import xyz.eginez.yacta.plugin.oci.createTerraformProvisionerFor
import java.io.StringWriter
import java.io.Writer
import kotlin.reflect.KClass

//take a resource and a provisioner and create a graph of things that need to be provisioned
fun provision (rootResource: Resource): Writer {
    val provisioners =  mutableMapOf<KClass<*>, Provisioner<*>>()
    val writer = StringWriter()

    provisioners.put(CompartmentResource::class, createTerraformProvisionerFor<CompartmentResource, Compartment>(writer, CompartmentResource::class)!!)
    provisioners.put(VcnResource::class, createTerraformProvisionerFor<VcnResource, Vcn>(writer, VcnResource::class)!!)

    provisioningGraph(rootResource, provisioners)

    return writer
}

fun provisioningGraph(rootResource: Resource, provisioners: Map<KClass<*>, Provisioner<*>>) {
    val current = rootResource

    current.children().forEach { provisioningGraph(it, provisioners) }

    val provisioner = provisioners[current::class]
    provisioner?.doCreate(current)
}

