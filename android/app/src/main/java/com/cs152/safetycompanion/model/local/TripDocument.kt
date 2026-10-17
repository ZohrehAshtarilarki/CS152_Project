package com.cs152.safetycompanion.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * MODEL (phone database): the downloaded trip, saved as ONE JSON record.
 * Only one trip is saved at a time (id is always 1). No joins, no migrations.
 */
@Entity(tableName = "active_trip_document")
data class TripDocument(
    @PrimaryKey val id: Int = 1,
    val tripId: String,
    val rawJson: String,
    /** When this phone last downloaded the trip. Shown as "last known info". */
    val lastSyncedUtc: Long,
)
