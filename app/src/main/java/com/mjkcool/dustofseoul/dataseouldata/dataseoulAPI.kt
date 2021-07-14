package com.mjkcool.dustofseoul.dataseouldata

import com.mjkcool.dustofseoul.key.apikey
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

class dataseoulAPI {
    companion object{
        const val BASE_URL = "http://openapi.seoul.go.kr:8088/"
        const val API_KEY = apikey.DATA_SEOUL
    }
}

interface dataSeoulAPIService{
    @GET("{api_key}/json/TimeAverageAirQuality/1/1/{formatting}/{gu}")
    fun getApiQulity(@Path("api_key") key: String,
                     @Path("formatting") format: String,
                     @Path("gu") gu: String): Call<TimeAverageAirQuality>
}

object dataSeoulAPIRetrofitClient{
    private val retrofit: Retrofit.Builder by lazy {
        Retrofit.Builder().baseUrl(dataseoulAPI.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
    }
    val apiService: dataSeoulAPIService by lazy {
        retrofit.build().create(dataSeoulAPIService::class.java)
    }
}