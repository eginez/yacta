package com.eginez.yacta.data

interface ResourceListener {
    fun preCreate()
    fun postCreate()
    fun postDestroy()
    fun preDestroy()
    fun preUpdate()
    fun postUpdate()
}