package com.bcgroup.notification

import com.bcgroup.notification.Cons.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiUtils {
    var retrofit: Retrofit? = null
    val client: ApiInterface
        get() {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit?.create(ApiInterface::class.java)!!
        }
}