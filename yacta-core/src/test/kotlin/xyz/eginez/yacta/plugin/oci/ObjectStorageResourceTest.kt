package xyz.eginez.yacta.plugin.oci

import com.oracle.bmc.Region
import  com.oracle.bmc.objectstorage.model.CreateBucketDetails.PublicAccessType.ObjectRead
import org.junit.Ignore
import org.junit.Test
import java.io.File

class ObjectStorageResourceTest {

    @Test
    @Ignore
    fun testCreateBucket() {
        val cid = "ocid1.compartment.oc1..aaaaaaaaptqakzgdmjxr4oq6f6v3vtoc5t3j44frmjf6snlm5zgfwo6lwkua"
        val c = Oci(Region.US_PHOENIX_1, cid)

        ////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////

        c.objectStorage {
            bucket {
                name = "EGZBucketOne"
                compartmentId = "ocid1.compartment.oc1..aaaaaaaaptqakzgdmjxr4oq6f6v3vtoc5t3j44frmjf6snlm5zgfwo6lwkua"
                accessType = ObjectRead
            }
        }

        ////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////
    }

    @Test
    @Ignore
    fun testCreateMultiple() {
        val cid = "ocid1.compartment.oc1..aaaaaaaaptqakzgdmjxr4oq6f6v3vtoc5t3j44frmjf6snlm5zgfwo6lwkua"
        val c = Oci(Region.US_PHOENIX_1, cid)

        ////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////

        c.objectStorage {
            (1..5).forEach { num ->
                bucket {
                    name = "NewDSLBucket$num"
                    compartmentId = "ocid1.compartment.oc1..aaaaaaaa6amlftmhyeeeil54oybmd5rizcruqirk73lcm3n45a5sib5ucbxa"
                    accessType = ObjectRead
                }
            }
        }

        ////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////
    }

    @Test
    @Ignore
    fun testBucketAndObject() {
        val cid = "ocid1.compartment.oc1..aaaaaaaaptqakzgdmjxr4oq6f6v3vtoc5t3j44frmjf6snlm5zgfwo6lwkua"
        val c = Oci(Region.US_PHOENIX_1, cid)

        ////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////

        c.objectStorage {
            bucket {
                name = "DSLBucketOneFile"
                compartmentId = "ocid1.compartment.oc1..aaaaaaaa6amlftmhyeeeil54oybmd5rizcruqirk73lcm3n45a5sib5ucbxa"
                accessType = ObjectRead
                obj {
                    file = File("/Users/eginez/Documents/images/nightsky_1.jpeg")
                    /*
                    OR add details manually
                    name = "filename"
                    length = 20
                    content = "asdfasdfasdfadf"
                    */

                }
            }
        }
        ////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////
    }

    @Test
    @Ignore
    fun testBucketAndMultipleObject() {
        val cid = "ocid1.compartment.oc1..aaaaaaaaptqakzgdmjxr4oq6f6v3vtoc5t3j44frmjf6snlm5zgfwo6lwkua"
        val c = Oci(Region.US_PHOENIX_1, cid)

        ////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////

        c.objectStorage {
            bucket {
                name = "DSLBucketFiles"
                compartmentId = "ocid1.compartment.oc1..aaaaaaaa6amlftmhyeeeil54oybmd5rizcruqirk73lcm3n45a5sib5ucbxa"
                accessType = ObjectRead
                File("/Users/eginez/Documents/images/").listFiles().forEach {
                    obj {
                        file = it
                    }
                }
                //or folder = "/Users/eginez/Documents/images"

            }
        }
        ////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////
    }
}