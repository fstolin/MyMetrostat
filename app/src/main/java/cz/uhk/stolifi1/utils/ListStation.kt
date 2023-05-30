package cz.uhk.stolifi1.utils

data class ListStation (

    var name: String,
    var dbId: Int,
    var distance: Double,
    var line: String = "",
    var lon: Double,
    var lat: Double

)