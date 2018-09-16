package com.eginez.yacta.data

import java.io.File


fun String.asFile(): File {
    val expanded = this.replaceFirst("~", System.getenv("HOME"))
    return File(expanded)
}