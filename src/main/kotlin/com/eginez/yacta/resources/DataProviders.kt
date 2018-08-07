package com.eginez.yacta.resources

interface DataProvider<out T> {
    fun get(): T
}