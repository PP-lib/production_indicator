package com.example.productionindicator.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "production_records")
data class ProductionRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val operatorId: String,
    val itemCode: String,
    val quantity: Int,
    val terminalTime: Long, // epoch milli
    val serverId: Long? = null,
    val synced: Boolean = false,
    val createdAt: Long = Instant.now().toEpochMilli()
)
