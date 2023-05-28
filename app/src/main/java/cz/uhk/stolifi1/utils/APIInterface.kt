package cz.uhk.stolifi1.utils

import cz.uhk.stolifi1.stations.Stations
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

// general interface for API calls
interface APIInterface {

    @GET
    fun getData(@Url url: String): Call<Stations>

}