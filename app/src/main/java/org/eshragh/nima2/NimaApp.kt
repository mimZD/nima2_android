package org.eshragh.nima2

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eshragh.nima2.data.local.AppDatabase
import org.eshragh.nima2.data.pref.UserPreferencesRepository
import org.eshragh.nima2.data.repository.CardRepository

class NimaApp : Application() {
    lateinit var cardRepository: CardRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getDatabase(this)
        val userPreferencesRepository = UserPreferencesRepository(this)
        cardRepository = CardRepository(
            context = this,
            cardDao = database.cardDao(),
            serverKartablDao = database.serverKartablDao(),
            metadataDao = database.metadataDao(),
            userPreferencesRepository = userPreferencesRepository
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Pre-warm database
                database.openHelper.writableDatabase
            } catch (_: Exception) {}
        }
    }
}
