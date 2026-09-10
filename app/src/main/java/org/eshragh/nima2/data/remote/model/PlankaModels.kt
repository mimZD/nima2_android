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
    val type: String,
    val data: com.google.gson.JsonElement? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

fun PlankaAttachment.extractUrl(): String? {
    if (data == null || !data.isJsonObject) return null
    val obj = data.asJsonObject
    val element = obj.get("url")
    return if (element != null && element.isJsonPrimitive) element.asString else null
}

fun PlankaAttachment.extractThumbnailUrl(): String? {
    if (data == null || !data.isJsonObject) return null
    val obj = data.asJsonObject
    val thumbsElement = obj.get("thumbnailUrls")
    if (thumbsElement != null && thumbsElement.isJsonObject) {
        val thumbs = thumbsElement.asJsonObject
        val o360 = thumbs.get("outside360")
        if (o360 != null && o360.isJsonPrimitive) return o360.asString
        val o720 = thumbs.get("outside720")
        if (o720 != null && o720.isJsonPrimitive) return o720.asString
    }
    return null // Return null if no actual thumbnail exists, to trigger icon fallback
}

fun PlankaAttachment.extractMimeType(): String? {
    if (data == null || !data.isJsonObject) return null
    val obj = data.asJsonObject
    val element = obj.get("mimeType")
    return if (element != null && element.isJsonPrimitive) element.asString else null
}

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
