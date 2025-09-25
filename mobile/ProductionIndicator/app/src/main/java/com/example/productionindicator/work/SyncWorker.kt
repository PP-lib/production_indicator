package com.example.productionindicator.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.productionindicator.data.AppDatabase
import kotlinx.coroutines.delay

class SyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val db = AppDatabase.get(applicationContext)
        val dao = db.productionRecordDao()
        val unsynced = dao.getUnsynced()
        if (unsynced.isEmpty()) return Result.success()
        // TODO: API連携実装 (Retrofit or Ktor). ここではスタブで待機後成功扱い。
        delay(300)
        // 本来はサーバID返却で markSynced を呼ぶ
        return Result.success()
    }
}
