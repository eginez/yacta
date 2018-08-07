package com.eginez.yacta.resources

interface Resource {

    @Throws(Exception::class)
    fun id(): String

    @Throws(Exception::class)
    fun create()

    //@Throws(Exception::class)
    //fun prepare()

    @Throws(Exception::class)
    fun destroy()

    @Throws(Exception::class)
    fun get(): Resource

    @Throws(Exception::class)
    fun update()

    @Throws(Exception::class)
    fun dependencies(): List<Resource>

}