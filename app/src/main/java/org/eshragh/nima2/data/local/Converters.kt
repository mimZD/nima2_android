package org.eshragh.nima2.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String {
        return status.name
    }

    @TypeConverter
    fun toSyncStatus(status: String): SyncStatus {
        return try {
            SyncStatus.valueOf(status)
        } catch (_: Exception) {
            SyncStatus.PENDING
        }
    }
}
