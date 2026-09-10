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

        try {
            repo.uploadPendingCards(serverUrl, token)
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}
