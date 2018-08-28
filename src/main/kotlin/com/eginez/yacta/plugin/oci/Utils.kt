package com.eginez.yacta.plugin.oci

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