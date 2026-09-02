package org.eshragh.nima2.data.remote.model

import com.google.gson.JsonElement

data class LoginRequest(
    val emailOrUsername: String,
    val password: String
)

data class AccessTokenResponse(
    val item: JsonElement?
)

data class CreateCardRequest(
    val name: String,
    val type: String = "story",
    val position: Double? = 65535.0
)

data class CardResponse(
    val item: PlankaCard?
)

data class PlankaCard(
    val id: String,
    val name: String,
    val listId: String
)

data class ProjectsResponse(
    val items: List<PlankaProject>?,
    val included: PlankaIncluded?
)

data class PlankaProject(
    val id: String,
    val name: String
)

data class PlankaBoard(
    val id: String,
    val projectId: String?,
    val name: String
)

data class PlankaList(
    val id: String,
    val boardId: String,
    val name: String?,
    val type: String? = null,
    val position: Double? = null
)

data class PlankaIncluded(
    val projects: List<PlankaProject>? = null,
    val boards: List<PlankaBoard>? = null,
    val lists: List<PlankaList>? = null
)

data class BoardDetailsResponse(
    val item: PlankaBoard?,
    val included: PlankaIncluded?
)
