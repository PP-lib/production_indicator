package com.example.productionindicator.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

class ProductionRepository(private val dao: ProductionRecordDao) {
    suspend fun record(operatorId: String, itemCode: String, quantity: Int) = withContext(Dispatchers.IO) {
        val now = Instant.now().toEpochMilli()
        dao.insert(
            ProductionRecord(
                operatorId = operatorId,
                itemCode = itemCode,
                quantity = quantity,
                terminalTime = now
            )
        )
    }

    suspend fun recent() = withContext(Dispatchers.IO) { dao.recent20() }
    suspend fun total() = withContext(Dispatchers.IO) { dao.totalAll() ?: 0 }
    suspend fun unsynced() = withContext(Dispatchers.IO) { dao.getUnsynced() }
}
