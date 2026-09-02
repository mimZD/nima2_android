package org.eshragh.nima2.data.remote

import org.eshragh.nima2.data.remote.model.AccessTokenResponse
import org.eshragh.nima2.data.remote.model.BoardDetailsResponse
import org.eshragh.nima2.data.remote.model.CardResponse
import org.eshragh.nima2.data.remote.model.CreateCardRequest
import org.eshragh.nima2.data.remote.model.LoginRequest
import org.eshragh.nima2.data.remote.model.ProjectsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

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
}
