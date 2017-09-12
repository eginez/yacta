package com.eginez.yacta

import com.oracle.bmc.Region
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.objectstorage.ObjectStorageClient
import com.oracle.bmc.objectstorage.model.CreateBucketDetails
import com.oracle.bmc.objectstorage.requests.CreateBucketRequest
import com.oracle.bmc.objectstorage.requests.GetNamespaceRequest
import com.oracle.bmc.objectstorage.requests.PutObjectRequest
import java.io.File

@DslMarker
annotation class ResourceMarker


val executionGraph : MutableList<Creatable> = mutableListOf()

interface Creatable {
    fun create()
}

@ResourceMarker
class Resource{

    var profile = "DEFAULT"
    var filePath = "~/.oraclebmc/config"
    var provider = ConfigFileAuthenticationDetailsProvider(filePath, profile)
    val DEFAULT_REGION = Region.US_PHOENIX_1
    var region: Region? = null


    fun casper(fn: Resource.() -> Unit) {
        fn()
        println(executionGraph)
        executionGraph.forEach { it.create() }
    }

    fun bucket(fn: BucketResource.() -> Unit) {
        val client = ObjectStorageClient(provider)
        client.setRegion(region)
        val n = BucketResource(client)
        executionGraph.add(n)
        n.apply(fn)
    }
}


class BucketResource(val client: ObjectStorageClient): Creatable {
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

    override fun create() {
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
        executionGraph.add(o)
    }

}

class ObjectResource(val client: ObjectStorageClient, val parentBucket: BucketResource): Creatable {
    var name: String = ""
    var namespace: String? = null
    var file: File? = null

    override fun create() {
        if (file == null) {
            throw IllegalArgumentException("file property has to be set")
        }


        println("Creating object ${this}")
        val request = PutObjectRequest.builder()
                .bucketName(parentBucket.name)
                .namespaceName(parentBucket.namespace)
                .objectName(file?.name)
                .contentLength(file?.length())
                .putObjectBody(file?.inputStream())
                .build()

        client.putObject(request)
    }

    override fun toString(): String {
        return "ObjectResource(parentBucket=$parentBucket, name='$name', namespace=$namespace, file=$file)"
    }
}



