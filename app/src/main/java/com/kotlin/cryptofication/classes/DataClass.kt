package com.kotlin.cryptofication.classes

class DataClass {
    companion object {
        lateinit var db: DatabaseClass
        var oldItem: Int = -25
        var newItem: Int = -25
        var firstRun: Boolean = true

    }
}