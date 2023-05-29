package cz.uhk.stolifi1.utils

data class ListStation (

    var name: String,
    var dbId: Long,
    var distance: Double,
    var line: String = "",

)