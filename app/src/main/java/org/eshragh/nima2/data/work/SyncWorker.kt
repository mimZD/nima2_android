package org.eshragh.nima2.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.firstOrNull
import org.eshragh.nima2.NimaApp
import org.eshragh.nima2.data.local.SyncStatus
import org.eshragh.nima2.data.remote.RetrofitClient
import org.eshragh.nima2.data.remote.model.CreateCardRequest
import org.eshragh.nima2.data.remote.model.AddCardLabelRequest
import org.eshragh.nima2.data.remote.model.CreateLabelRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as NimaApp
        val repo = app.cardRepository
        val prefs = repo.getUserPreferencesRepository()
        
        val serverUrl = prefs.serverUrl.firstOrNull() ?: return Result.failure()
        val token = prefs.authToken.firstOrNull() ?: return Result.failure()
        val authHeader = "Bearer $token"
        val api = RetrofitClient.getApi(serverUrl)

        val pendingCards = repo.getPendingCardsSync()

        for (card in pendingCards) {
            try {
                repo.updateCardStatus(card, SyncStatus.UPLOADING)

                val createRes = api.createCard(
                    listId = card.listId,
                    authHeader = authHeader,
                    request = CreateCardRequest(name = card.title, type = "project", dueDate = card.dueDate)
                )

                val createdCard = createRes.body()?.item
                if (createRes.isSuccessful && createdCard != null) {
                    // Labels
                    val labelIds = card.labelIds?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
                    for (lId in labelIds) {
                        try {
                            api.addCardLabel(createdCard.id, authHeader, AddCardLabelRequest(lId))
                        } catch (_: Exception) {}
                    }

                    // Attachments
                    val paths = card.localAttachmentPaths?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
                    for (path in paths) {
                        try {
                            repo.uploadServerAttachment(serverUrl, token, createdCard.id, path)
                            // Note: uploadServerAttachment handles file existence and multipart creation
                            val file = File(path)
                            if (file.exists()) file.delete()
                        } catch (_: Exception) {}
                    }

                    repo.updateCardStatus(card, SyncStatus.SYNCED)
                    // Delete synced card from local DB to keep it clean
                    repo.deleteOfflineCard(card)
                } else {
                    repo.updateCardStatus(card, SyncStatus.FAILED, "Error ${createRes.code()}")
                }
            } catch (e: Exception) {
                repo.updateCardStatus(card, SyncStatus.FAILED, e.localizedMessage)
            }
        }

        return Result.success()
    }
}
