package com.cs152.safetycompanion.model.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDocumentDao {

    /** Emits again every time the saved trip changes. */
    @Query("SELECT * FROM active_trip_document WHERE id = 1 LIMIT 1")
    fun observe(): Flow<TripDocument?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(document: TripDocument)

    @Query("DELETE FROM active_trip_document")
    suspend fun clear()
}
