package com.eginez.yacta

import com.eginez.yacta.resources.Oci
import  com.oracle.bmc.objectstorage.model.CreateBucketDetails.PublicAccessType.*
import org.junit.Ignore
import org.junit.Test
import java.io.File

class ObjectStorageResourceTest {

    @Test
    @Ignore
    fun testCreateBucket() {
        val c = Oci()

        ////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////

        c.objectStorage {
            region = DEFAULT_REGION
            bucket {
                name = "DSLBucketOne"
                compartmentId = "ocid1.compartment.oc1..aaaaaaaa6amlftmhyeeeil54oybmd5rizcruqirk73lcm3n45a5sib5ucbxa"
                accessType = ObjectRead
            }
        }

        ////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////
    }

    @Test
    @Ignore
    fun testCreateMultiple() {
        val c = Oci()

        ////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////

        c.objectStorage {
            region = DEFAULT_REGION
            (1..5).forEach { num ->
                bucket {
                    name = "DSLBucket$num"
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
        val c = Oci()

        ////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////

        c.objectStorage {
            region = DEFAULT_REGION
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
        val c = Oci()

        ////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////

        c.objectStorage {
            region = DEFAULT_REGION
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