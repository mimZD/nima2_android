package org.eshragh.nima2.data.remote

import okhttp3.ResponseBody
import org.eshragh.nima2.data.remote.model.CheckUpdateRequest
import org.eshragh.nima2.data.remote.model.CheckUpdateResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Streaming
import retrofit2.http.Url

interface AppUpdateApi {

    @POST("api/v1/update/check")
    suspend fun checkUpdate(
        @Body request: CheckUpdateRequest
    ): Response<CheckUpdateResponse>

    @Streaming
    @GET
    suspend fun downloadApk(
        @Url url: String
    ): Response<ResponseBody>
}
