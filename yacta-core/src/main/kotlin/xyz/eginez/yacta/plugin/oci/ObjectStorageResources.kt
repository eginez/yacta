package xyz.eginez.yacta.plugin.oci


import com.oracle.bmc.model.BmcException
import com.oracle.bmc.objectstorage.ObjectStorageClient
import com.oracle.bmc.objectstorage.model.Bucket
import com.oracle.bmc.objectstorage.model.CreateBucketDetails
import com.oracle.bmc.objectstorage.requests.*
import xyz.eginez.yacta.data.Resource
import java.io.File
import java.io.InputStream

class BucketResource(val client: ObjectStorageClient) : Resource<Bucket> {

    var name: String = ""
    var compartmentId: String = ""
    var accessType: CreateBucketDetails.PublicAccessType? = null
    var namespace: String? = null
    private var id: String? = null

    override fun toString(): String {
        return "BucketResource(namespace='${namespace}' name='$name', compartmentId='$compartmentId', accessType=$accessType)"
    }

    fun defaultNamespace(): String {
        return client.getNamespace(GetNamespaceRequest.builder().build()).value
    }

    override fun id(): String {
        return name
    }

    override fun dependencies(): List<Resource<*>> {
        return emptyList()
    }

    fun isPresent(): Boolean {
        val request = GetBucketRequest.builder()
                .bucketName(name)
                .namespaceName(namespace)
                .build()
        try {
            val response = client.getBucket(request)
            return true
        } catch (ex: BmcException) {
            println("Failed to get bucket: ${ex.statusCode}")
            return false
        }
    }

    override fun create() {
        if (namespace.isNullOrBlank()) {
            namespace = defaultNamespace()
        }

        if (isPresent()) {
            println("Bucket already there. Skipping creation")
            return
        }

        println("Creating bucket ${this}")
        val details = CreateBucketDetails(name, compartmentId, emptyMap(), accessType,
                CreateBucketDetails.StorageTier.Standard, emptyMap(), emptyMap())
        val request = CreateBucketRequest.builder()
                .namespaceName(namespace)
                .createBucketDetails(details)
                .build()
        client.createBucket(request)
    }

    override fun destroy() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    fun obj(fn: ObjectResource.() -> Unit) {
        val o = ObjectResource(client, this)
        o.apply(fn)
        executionGraph.add(o)
    }

    override fun get(): Bucket {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

class ObjectResource(val client: ObjectStorageClient, val parentBucket: BucketResource) : Resource<InputStream> {
    var name: String = ""
    var namespace: String? = null
    var file: File? = null

    override fun id(): String {
        return name
    }

    override fun dependencies(): List<Resource<*>> {
        return listOf(parentBucket)
    }

    override fun create() {
        if (file == null) {
            throw IllegalArgumentException("file property has to be set")
        }


        if (!isDifferent()) {
            println("Skip creating object")
            return
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

    override fun destroy() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun isDifferent(): Boolean {
        val request = GetObjectRequest.builder()
                .bucketName(parentBucket.name)
                .namespaceName(parentBucket.namespace)
                .objectName(file?.name)
                .build()
        try {
            val response = client.getObject(request)
            return response.contentLength != file?.length()
        } catch (ex: BmcException) {
            println("Failed to get bucket: ${ex.statusCode}")
            if (ex.statusCode in 400..499) {
                return true
            }
            throw ex
        }
    }

    override fun get(): InputStream {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun update() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun toString(): String {
        return "ObjectResource(parentBucket=$parentBucket, name='$name', namespace=$namespace, file=$file)"
    }
}
