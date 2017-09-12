package com.eginez.yacta

import com.oracle.bmc.Region
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.core.ComputeClient
import com.oracle.bmc.core.model.LaunchInstanceDetails
import com.oracle.bmc.core.requests.LaunchInstanceRequest
import com.oracle.bmc.objectstorage.ObjectStorageClient
import com.oracle.bmc.objectstorage.model.CreateBucketDetails
import com.oracle.bmc.objectstorage.requests.CreateBucketRequest
import com.oracle.bmc.objectstorage.requests.GetNamespaceRequest
import com.oracle.bmc.objectstorage.requests.PutObjectRequest
import java.io.File

@DslMarker
annotation class ResourceMarker


val executionGraph : MutableList<Any> = mutableListOf()
@ResourceMarker
class Resource {

    var profile = "DEFAULT"
    var filePath = "~/.oraclebmc/config"
    var provider = ConfigFileAuthenticationDetailsProvider(filePath, profile)
    val DEFAULT_REGION = Region.US_PHOENIX_1
    var region: Region? = null



    fun compute(fn: Resource.() -> Unit): Unit {
        println("Creating compute resource");
    }

    fun casper(fn: Resource.() -> Unit) {
        fn()

    }

    fun bucket(fn: BucketResource.() -> Unit) {
        val client = ObjectStorageClient(provider)
        client.setRegion(region)
        val n = BucketResource(client)
        n.apply(fn)
        n.create()
    }
}


data class BucketResource(val client: ObjectStorageClient) {
    var name: String = ""
    var compartmentId: String = ""
    var accessType : CreateBucketDetails.PublicAccessType? = null
    var namespace: String? = null

    override fun toString(): String {
        return "BucketResource(namespace='${namespace}' name='$name', compartmentId='$compartmentId', accessType=$accessType)"
    }

    fun defaultNamespace(): String {
        return client.getNamespace(GetNamespaceRequest.builder().build()).value
    }

    fun create() {
        if(namespace.isNullOrBlank()) {
            namespace = defaultNamespace()
        }

        println("Creating bucket ${this}")
        val details = CreateBucketDetails(name, compartmentId, emptyMap(), accessType)
        val request = CreateBucketRequest.builder()
                .namespaceName(namespace)
                .createBucketDetails(details)
                .build()
        client.createBucket(request)
    }

    fun obj(fn: ObjectResource.() -> Unit) {
        val o = ObjectResource(client, this)
        o.apply(fn)
        o.create()

    }

}

data class ObjectResource(val client: ObjectStorageClient, val parentBucket: BucketResource) {
    var name: String = ""
    var accessType : CreateBucketDetails.PublicAccessType? = null
    var namespace: String? = null
    var file = File("/Users/eginez/Documents/images/nightsky_1.jpeg")

    fun create() {
        val request = PutObjectRequest.builder()
                .bucketName(parentBucket.name)
                .namespaceName(parentBucket.namespace)
                .objectName(name)
                .contentLength(file.length())
                .putObjectBody(file.inputStream())
                .build()

        client.putObject(request)
    }
}



