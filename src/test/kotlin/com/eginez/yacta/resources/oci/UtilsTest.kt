package com.eginez.yacta.resources.oci

import com.oracle.bmc.Region
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.core.ComputeClient
import com.oracle.bmc.core.model.Image
import com.oracle.bmc.core.requests.ListImagesRequest
import org.junit.Test

class UtilsTest {

    @Test
    fun TestFullyList() {

        val configuration = ConfigFileAuthenticationDetailsProvider("~/.oci/config", "DEFAULT")
        val client = ComputeClient(configuration)
        client.setRegion(Region.US_PHOENIX_1)
        val images = fullyList<Image, ListImagesRequest>({ page ->
            ListImagesRequest.builder()
                    .compartmentId(configuration.tenantId)
                    .page(page)
                    .build()
        }, { r: ListImagesRequest ->
            val response = client.listImages(r)
            Pair(response.opcNextPage, response.items)
        })
        assert(images.isEmpty() == false)
    }
}
