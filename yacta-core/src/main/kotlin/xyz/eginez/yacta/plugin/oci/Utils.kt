package xyz.eginez.yacta.plugin.oci

import com.oracle.bmc.Region
import com.oracle.bmc.auth.AuthenticationDetailsProvider
import com.oracle.bmc.common.RegionalClientBuilder

fun <T,R> fullyList(createRequestFn:(String?) -> R, listFn:(R) ->Pair<String?,List<T>> ): List<T> {
    val allItems = mutableListOf<T>()
    var page:String? = null

    while(true) {
        val request = createRequestFn(page)
        val (nextPage, items) = listFn(request)
        allItems.addAll(items)
        if (nextPage == null) {
            break
        }
        page = nextPage
    }
    return allItems
}

fun <T,R,S> fullyPaginate(request: T, paginator: (T)->Iterable<R>, itemizer:(R)->List<S>): Set<S> {
    val iterable = paginator(request)
    val elements = mutableSetOf<S>()
    iterable.forEach { elements.addAll(itemizer(it)) }
    return elements
}

fun <T> createClient(provider: AuthenticationDetailsProvider,
                     region: Region, regionalClientBuilder: RegionalClientBuilder<*,T>,
                     init: (T) -> Unit = {}): T {
    regionalClientBuilder.region(region)
    var client = regionalClientBuilder.build(provider)
    client.apply(init)
    return client
}