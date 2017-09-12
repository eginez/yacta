package com.eginez.yacta

import org.junit.Test

class ResourceTest {
    @Test
    fun testBasic() {
        val c = Resource()
        c.compute {
            assert(true)
        }
    }
}