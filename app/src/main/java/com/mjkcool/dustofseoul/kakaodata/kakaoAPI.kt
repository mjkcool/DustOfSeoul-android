package com.mjkcool.dustofseoul.kakaodata;

import com.mjkcool.dustofseoul.key.apikey
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

class KakaoAPI{
    companion object{
        const val BASE_URL = "https://dapi.kakao.com/"
        const val API_KEY = "KakaoAK ${apikey.KAKAO}"
    }
}

interface kakaoAPIService{
    @GET("v2/local/geo/coord2regioncode.json")
    fun getApiGu(
        @Header("Authorization") key: String,
        @Query("x") x: String,
        @Query("y") y: String
    ): Call<Coord2regioncode>
}

object kakaoAPIRetrofitClient{
    private val retrofit: Retrofit.Builder by lazy {
        Retrofit.Builder().baseUrl(KakaoAPI.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
    }

    val apiService: kakaoAPIService by lazy {
        retrofit.build().create(kakaoAPIService::class.java)
    }
}