package cz.uhk.stolifi1.stations

data class StopGroup(
    val avgJtskX: Double,
    val avgJtskY: Double,
    val avgLat: Double,
    val avgLon: Double,
    val cis: Int,
    val districtCode: String,
    val fullName: String,
    val idosCategory: Int,
    val idosName: String,
    val isTrain: Boolean,
    val municipality: String,
    val name: String,
    val node: Int,
    val stops: List<Stop>,
    val uniqueName: String
)