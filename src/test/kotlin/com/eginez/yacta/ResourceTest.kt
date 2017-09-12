package com.eginez.yacta

import  com.oracle.bmc.objectstorage.model.CreateBucketDetails.PublicAccessType.*
import org.junit.Test
import java.io.File

class ResourceTest {

    @Test
    fun testCreateBucket() {
        val c = Resource()

        ////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////

        c.casper {
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
    fun testCreateMultiple() {
        val c = Resource()

        ////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////

        c.casper {
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
    fun testBucketAndObject() {
        val c = Resource()

        ////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////

        c.casper {
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
    fun testBucketAndMultipleObject() {
        val c = Resource()

        ////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////

        c.casper {
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