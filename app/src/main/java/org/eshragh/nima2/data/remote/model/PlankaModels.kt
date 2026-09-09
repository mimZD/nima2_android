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
    val type: String = "project",
    val position: Double? = 65535.0,
    val dueDate: String? = null
)

data class UpdateCardRequest(
    val name: String? = null,
    val boardId: String? = null,
    val listId: String? = null,
    val dueDate: String? = null,
    val position: Double? = 65535.0
)

data class CreateLabelRequest(
    val name: String,
    val color: String = "blue-xchange"
)

data class AddCardLabelRequest(
    val labelId: String
)

data class LabelResponse(
    val item: PlankaLabel?
)

data class CardResponse(
    val item: PlankaCard?
)

data class PlankaAttachment(
    val id: String,
    val cardId: String,
    val name: String,
    val url: String? = null,
    val coverUrl: String? = null
)

data class PlankaCardLabel(
    val id: String,
    val cardId: String,
    val labelId: String
)

data class PlankaCard(
    val id: String,
    val name: String,
    val listId: String,
    val boardId: String? = null,
    val dueDate: String? = null,
    val isClosed: Boolean? = null,
    val attachmentsCount: Int? = 0
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

data class PlankaLabel(
    val id: String,
    val boardId: String,
    val name: String?,
    val color: String?
)

data class PlankaIncluded(
    val projects: List<PlankaProject>? = null,
    val boards: List<PlankaBoard>? = null,
    val lists: List<PlankaList>? = null,
    val cards: List<PlankaCard>? = null,
    val labels: List<PlankaLabel>? = null,
    val cardLabels: List<PlankaCardLabel>? = null,
    val attachments: List<PlankaAttachment>? = null
)

data class BoardDetailsResponse(
    val item: PlankaBoard?,
    val included: PlankaIncluded?
)
