package xyz.eginez.yacta.plugin.oci


import com.oracle.bmc.Region
import com.oracle.bmc.auth.AuthenticationDetailsProvider
import xyz.eginez.yacta.data.Resource
import com.oracle.bmc.objectstorage.model.CreateBucketDetails
import com.oracle.bmc.objectstorage.requests.*
import com.oracle.bmc.model.BmcException
import com.oracle.bmc.objectstorage.ObjectStorageClient
import com.oracle.bmc.objectstorage.model.Bucket
import com.oracle.bmc.objectstorage.model.UpdateBucketDetails
import java.io.File
import java.io.InputStream

class BucketResource (configurationProvider: AuthenticationDetailsProvider,
                      region: Region,
                      compartment: CompartmentResource?): OciBaseResource<Bucket>(configurationProvider, region, compartment){

    private val client = createClient<ObjectStorageClient>(configurationProvider, region, ObjectStorageClient.builder())
    var name: String = ""
    var accessType : CreateBucketDetails.PublicAccessType? = null
    var namespace: String? = null
    private var id: String? = null
    var objects = mutableSetOf<ObjectResource>()

    override fun toString(): String {
        return "BucketResource(namespace='${namespace}' name='$name', compartmentId='$compartment', accessType=$accessType)"
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

    override fun doCreate() {
        if(namespace.isNullOrBlank()) {
            namespace = defaultNamespace()
        }
        val details = CreateBucketDetails(name, compartment?.id(), emptyMap(), accessType,
                CreateBucketDetails.StorageTier.Standard, emptyMap(), emptyMap() )
        val request = CreateBucketRequest.builder()
                .namespaceName(namespace)
                .createBucketDetails(details)
                .build()
        client.createBucket(request)
        objects.forEach { it.create() }
    }

    override fun doDestroy() {
        objects.forEach { it.doDestroy() }
        val req = DeleteBucketRequest.builder()
                .bucketName(name)
                .namespaceName(namespace)
                .build()
        client.deleteBucket(req)
    }

    override fun get(): Bucket {
        val req = GetBucketRequest.builder()
                .bucketName(name)
                .namespaceName(namespace)
                .build()
        val response = client.getBucket(req)
        return response.bucket
    }

    override fun doUpdate() {
        val req = UpdateBucketRequest.builder()
                .bucketName(name)
                .updateBucketDetails(UpdateBucketDetails.builder()
                        .compartmentId(compartment?.id()).build())
                .build()
        client.updateBucket(req)
    }

    fun file(f: File){
        val o = ObjectResource(configurationProvider, region, compartment)
        o.bucket = this
        o.file = f
        objects.add(o)
    }
}

fun Oci.bucket(provider: AuthenticationDetailsProvider= this.provider,
            region: Region = this.region,
            compartment: CompartmentResource? = this.compartment,
            fn: BucketResource.() -> Unit = {}): BucketResource {
    val v = BucketResource(provider, region, compartment)
    v.apply(fn)
    return v
}

class ObjectResource (configurationProvider: AuthenticationDetailsProvider,
                      region: Region,
                      compartment: CompartmentResource?): OciBaseResource<InputStream>(configurationProvider, region, compartment){
    var name: String = ""
    lateinit var bucket: BucketResource
    var file: File? = null
    private val client = createClient<ObjectStorageClient>(configurationProvider, region, ObjectStorageClient.builder())

    override fun id(): String {
        return "$bucket/$name"
    }

    override fun dependencies(): List<Resource<*>> {
        return emptyList()
    }

    override fun doCreate() {
        checkNotNull(file)

        name = file!!.name
        val request = PutObjectRequest.builder()
                .bucketName(bucket.name)
                .namespaceName(bucket.namespace)
                .objectName(name)
                .contentLength(file!!.length())
                .putObjectBody(file!!.inputStream())
                .build()

        client.putObject(request)
    }

    override fun doDestroy() {
        val req = DeleteObjectRequest.builder()
                .bucketName(bucket.name)
                .namespaceName(bucket.namespace)
                .objectName(name)
                .build()
        client.deleteObject(req)
    }

    override fun get(): InputStream {
        val req = GetObjectRequest.builder()
                .bucketName(bucket.name)
                .namespaceName(bucket.namespace)
                .objectName(name)
                .build()
        val res = client.getObject(req)
        return res.inputStream
    }

    override fun doUpdate() {
        throw IllegalStateException("Can not update object")
    }

    override fun toString(): String {
        return "ObjectResource(parentBucket=$bucket, name='$name', file=$file)"
    }
}

fun BucketResource.obj(fn: ObjectResource.() -> Unit) {
    val o = ObjectResource(configurationProvider, region, compartment)
    o.bucket = this
    this.objects.add(o)

}
