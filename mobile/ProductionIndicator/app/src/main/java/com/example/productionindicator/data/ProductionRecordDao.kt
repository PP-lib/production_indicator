package com.example.productionindicator.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProductionRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: ProductionRecord): Long

    @Query("SELECT * FROM production_records WHERE synced = 0 ORDER BY id ASC")
    suspend fun getUnsynced(): List<ProductionRecord>

    @Query("UPDATE production_records SET synced = 1, serverId = :serverId WHERE id = :id")
    suspend fun markSynced(id: Long, serverId: Long)

    @Query("SELECT SUM(quantity) FROM production_records")
    suspend fun totalAll(): Int?

    @Query("SELECT * FROM production_records ORDER BY id DESC LIMIT 20")
    suspend fun recent20(): List<ProductionRecord>
}
