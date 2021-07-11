package com.datartproject.dataart.client

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import java.util.*

class Uploader(appContext: Context,
               private val apiKey: String,
               private val actionsBatchSize: Int,
               private val backoffRatio: Long,
               private val numRetries: Int,
               private val flushInterval: Long) {

    private val actionsQueue = ActionsQueue(appContext)
    private val wManager = WorkManager.getInstance(appContext)

    fun startPeriodicFlush() {
        val req = PeriodicActionsFlushWork.RequestBuilder()
            .setApiKey(apiKey)
            .setBackoffRatio(backoffRatio)
            .setPeriodInterval(flushInterval)
            .build()
        wManager.enqueueUniquePeriodicWork(
            PeriodicActionsFlushWork.NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            req,
        )
    }

    fun uploadAction(action: Action) {
        actionsQueue.push(action)
        if (actionsQueue.size() == actionsBatchSize) {
            val actions = actionsQueue.drain(actionsBatchSize)
            val req = UploadWork.RequestBuilder()
                .setNumRetries(numRetries)
                .setApiKey(apiKey)
                .setPayloadType(UploadWork.PayloadType.Action)
                .setPayload(ActionsContainer(Date(), actions).toJsonString())
                .setBackoffRatio(backoffRatio)
                .build()
            wManager.enqueue(req)
        }
    }

    fun uploadIdentity(identity: Identity) {
        val req = UploadWork.RequestBuilder()
            .setNumRetries(numRetries)
            .setApiKey(apiKey)
            .setPayloadType(UploadWork.PayloadType.Identity)
            .setPayload(identity.toJsonString())
            .setBackoffRatio(backoffRatio)
            .build()
        wManager.enqueue(req)
    }
}
