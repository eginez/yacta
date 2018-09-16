package com.eginez.yacta.data

interface DataProvider<out T> {
    fun get(): T
}