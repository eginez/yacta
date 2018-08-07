package com.eginez.yacta.resources

interface ResourceListener {
    fun preCreate()
    fun postCreate()
    fun postDestroy()
    fun preDestroy()
    fun preUpdate()
    fun postUpdate()
}