package com.coffeeanddistractions.advancecoroutines

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/*
 * Created by Abdu on 12/17/2017.
 */

class RestClient{
    companion object {
        var retrofit: Retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://10.0.2.2:3000")
                .build()
        val apiDefinition: APIDefinition = retrofit.create(APIDefinition::class.java)
    }
}
