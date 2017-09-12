package com.eginez.yacta

import  com.oracle.bmc.objectstorage.model.CreateBucketDetails.PublicAccessType.*
import org.junit.Test

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
                /*
                obj {
                    name = "foto1.jpg"
                }
                */
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
                name = "DSLBucket"
                compartmentId = "ocid1.compartment.oc1..aaaaaaaa6amlftmhyeeeil54oybmd5rizcruqirk73lcm3n45a5sib5ucbxa"
                accessType = ObjectRead
                obj {
                    name = "foto1.jpg"
                }
            }
        }
        ////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////
    }
}