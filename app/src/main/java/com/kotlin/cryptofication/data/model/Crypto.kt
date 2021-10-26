package com.kotlin.cryptofication.data.model

data class Crypto(var id: String, var name: String, var symbol: String, var image: String,
                  var current_price: Double, var market_cap_rank: Int, var high_24h: Double,
                  var low_24h: Double, var price_change_percentage_24h: Double)

