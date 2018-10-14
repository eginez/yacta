package xyz.eginez.yacta.plugin.oci

import com.oracle.bmc.Region
import  com.oracle.bmc.objectstorage.model.CreateBucketDetails.PublicAccessType.ObjectRead
import org.junit.Ignore
import org.junit.Test
import xyz.eginez.yacta.data.asFile
import java.io.File
import java.nio.file.Paths

class ObjectStorageResourceTest {
    val configPath = Paths.get("~/.oci","config")
    val compartmentId = System.getenv("COMPARTMENT_ID")

    @Test
    fun simpleBucket() {
        val oci = Oci(region = Region.US_PHOENIX_1, configFilePath = configPath.toString(), compartmentId = compartmentId)

        val bucket = oci.bucket {
            name = "sampleBucket"
            accessType = ObjectRead
            val allFiles = "~/Documents/".asFile().listFiles { f, n -> n.endsWith(".png") }
            file(allFiles[0])
        }

        bucket.create()

        println("Create bucket ${bucket.name} in ${bucket.namespace}")

        bucket.destroy()

        bucket.get()
    }
}