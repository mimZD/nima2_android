package org.eshragh.nima2.data.remote

import com.google.gson.JsonElement
import org.eshragh.nima2.data.remote.model.AccessTokenResponse
import org.eshragh.nima2.data.remote.model.AddCardLabelRequest
import org.eshragh.nima2.data.remote.model.BoardDetailsResponse
import org.eshragh.nima2.data.remote.model.CardResponse
import org.eshragh.nima2.data.remote.model.CreateCardRequest
import org.eshragh.nima2.data.remote.model.CreateLabelRequest
import org.eshragh.nima2.data.remote.model.LabelResponse
import org.eshragh.nima2.data.remote.model.LoginRequest
import org.eshragh.nima2.data.remote.model.ProjectsResponse
import org.eshragh.nima2.data.remote.model.UpdateCardRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import okhttp3.MultipartBody

interface PlankaApi {

    @POST("api/access-tokens")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<AccessTokenResponse>

    @GET("api/projects")
    suspend fun getProjects(
        @Header("Authorization") authHeader: String
    ): Response<ProjectsResponse>

    @GET("api/boards/{boardId}")
    suspend fun getBoardDetails(
        @Path("boardId") boardId: String,
        @Header("Authorization") authHeader: String
    ): Response<BoardDetailsResponse>

    @POST("api/lists/{listId}/cards")
    suspend fun createCard(
        @Path("listId") listId: String,
        @Header("Authorization") authHeader: String,
        @Body request: CreateCardRequest
    ): Response<CardResponse>

    @POST("api/boards/{boardId}/labels")
    suspend fun createLabel(
        @Path("boardId") boardId: String,
        @Header("Authorization") authHeader: String,
        @Body request: CreateLabelRequest
    ): Response<LabelResponse>

    @POST("api/cards/{cardId}/card-labels")
    suspend fun addCardLabel(
        @Path("cardId") cardId: String,
        @Header("Authorization") authHeader: String,
        @Body request: AddCardLabelRequest
    ): Response<JsonElement>

    @DELETE("api/cards/{cardId}")
    suspend fun deleteCard(
        @Path("cardId") cardId: String,
        @Header("Authorization") authHeader: String
    ): Response<JsonElement>

    @PATCH("api/cards/{cardId}")
    suspend fun updateCard(
        @Path("cardId") cardId: String,
        @Header("Authorization") authHeader: String,
        @Body request: org.eshragh.nima2.data.remote.model.UpdateCardRequest
    ): Response<org.eshragh.nima2.data.remote.model.CardResponse>

    @Multipart
    @POST("api/cards/{cardId}/attachments")
    suspend fun uploadAttachment(
        @Path("cardId") cardId: String,
        @Header("Authorization") authHeader: String,
        @Part file: List<okhttp3.MultipartBody.Part>
    ): Response<com.google.gson.JsonElement>

    @DELETE("api/attachments/{attachmentId}")
    suspend fun deleteAttachment(
        @Path("attachmentId") attachmentId: String,
        @Header("Authorization") authHeader: String
    ): Response<com.google.gson.JsonElement>
}
