package com.kotlin.cryptofication.utilities

import com.kotlin.cryptofication.data.DatabaseClass

class DataClass {
    companion object {
        lateinit var db: DatabaseClass
        var oldItem = -25
        var newItem = -25
        var firstRun = true
    }
}