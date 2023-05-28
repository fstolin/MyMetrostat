package cz.uhk.stolifi1.stations

data class Stop(
    val altIdosName: String?,
    val gtfsIds: List<String>?,
    val id: String?,
    val isMetro: Boolean?,
    val jtskX: Double?,
    val jtskY: Double?,
    val lat: Double?,
    val lines: List<Line>?,
    val lon: Double?,
    val platform: String?,
    val wheelchairAccess: String?,
    val zone: String?
)