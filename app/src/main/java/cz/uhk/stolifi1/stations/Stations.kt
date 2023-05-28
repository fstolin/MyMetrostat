package cz.uhk.stolifi1.stations

data class Stations(
    val dataFormatVersion: String,
    val generatedAt: String,
    val stopGroups: List<StopGroup>
)