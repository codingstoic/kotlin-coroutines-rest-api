package com.coffeeanddistractions.advancecoroutines

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST


/*
 * Created by Abdu on 12/16/2017.
 */

interface APIDefinition {
    @GET("users")
    fun listUsers(): Call<List<User>>

    @POST("users")
    fun createUser(user: User): Call<UserCreateResponse>

    @GET("posts")
    fun listPosts(): Call<List<Post>>
}

data class User(val first_name: String, val last_name: String)
data class Post(val id: Int, val title: String, val author: String)
data class UserCreateResponse(val id: Int)
