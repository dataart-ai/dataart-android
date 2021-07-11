package com.datartproject.dataart.client

import android.content.Context
import androidx.work.*
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit

class PeriodicActionsFlushWork
    (appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    private val actionsQueue = ActionsQueue(appContext)
    private val wManager = WorkManager.getInstance(appContext)

    companion object {
        const val NAME = "dataart/periodic-actions-flush-work"
        const val TAG = "dataart/actions"

        private const val keyApiKey = "apiKey"

        fun buildInputData(apiKey: String): Data {
            return workDataOf(
                keyApiKey to apiKey,
            )
        }
    }

    override fun doWork(): Result {
        ifLet(inputData.getString(keyApiKey)) { (apiKey) ->
            if (actionsQueue.size() > 0) {
                val actions = actionsQueue.drain(actionsQueue.size())
                val req = UploadWork.RequestBuilder()
                    .setApiKey(apiKey)
                    .setPayloadType(UploadWork.PayloadType.Action)
                    .setPayload(ActionsContainer(Date(), actions).toJsonString())
                    .build()
                wManager.enqueue(req)
            }

            return Result.success()
        }

        wManager.cancelUniqueWork(NAME)
        return Result.failure()
    }

    class RequestBuilder {
        private var apiKey: String? = null
        private var backoffDelay: Long? = null
        private var periodInterval: Long? = null

        private fun gotValidSpec(): Boolean {
            if (apiKey == null || backoffDelay == null || periodInterval == null) {
                return false
            }

            return true
        }

        fun setApiKey(key: String): RequestBuilder {
            apiKey = key
            return this
        }

        fun setBackoffRatio(value: Long): RequestBuilder {
            backoffDelay = value
            return this
        }

        fun setPeriodInterval(value: Long): RequestBuilder {
            periodInterval = value
            return this
        }

        private fun buildWorkConstraint(): Constraints {
            return Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        }

        @Throws(Exception::class)
        fun build(): PeriodicWorkRequest {
            if (!gotValidSpec()) {
                throw Exception("Provided values are not valid for periodic actions flush work")
            }

            return PeriodicWorkRequestBuilder<PeriodicActionsFlushWork>(
                periodInterval ?: throw Exception("periodInterval must not be null"),
                TimeUnit.MILLISECONDS
            )
                .setConstraints(buildWorkConstraint())
                .addTag(TAG)
                .setInputData(
                    buildInputData(
                        apiKey ?: throw Exception("apiKey must not be null"),
                    )
                )
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    backoffDelay ?: throw Exception("backoffDelay must not be null"),
                    TimeUnit.SECONDS
                )
                .build()
        }
    }
}

class UploadWork(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    enum class PayloadType {
        Action,
        Identity
    }

    companion object {
        const val TAG = "dataart/upload"

        private const val defaultNumRetries = 0

        private const val keyNumRetries = "numRetries"
        private const val keyApiKey = "apiKey"
        private const val keyPayloadType = "payloadType"
        private const val keyPayload = "payload"

        fun buildInputData(
            numRetries: Int,
            apiKey: String,
            payloadType: PayloadType,
            payload: String
        ): Data {
            return workDataOf(
                keyNumRetries to numRetries,
                keyApiKey to apiKey,
                keyPayloadType to payloadType.name,
                keyPayload to payload
            )
        }
    }

    override fun doWork(): Result {
        ifLet(
            inputData.getString(keyApiKey),
            inputData.getString(keyPayloadType),
            inputData.getString(keyPayload)
        ) { (apiKey, payloadType, payload) ->

            if (runAttemptCount > inputData.getInt(keyNumRetries, defaultNumRetries)) {
                return@doWork Result.failure()
            }

            val httpClient = DataArtHttpClient(apiKey)

            when (payloadType) {
                PayloadType.Action.name -> {
                    if (httpClient.postActions(payload).isSuccessful())
                        return@doWork Result.success()
                    return@doWork Result.retry()
                }
                PayloadType.Identity.name -> {
                    if (httpClient.postIdentity(payload).isSuccessful())
                        return@doWork Result.success()
                    return@doWork Result.retry()
                }
                else -> return@doWork Result.failure()
            }
        }

        return Result.failure()
    }

    class RequestBuilder {
        private var apiKey: String? = null
        private var payloadType: PayloadType? = null
        private var payload: String? = null
        private var backoffDelay: Long? = null
        private var numRetries: Int? = null

        private fun gotValidSpec(): Boolean {
            if (apiKey == null || payloadType == null || payload == null
                || backoffDelay == null || numRetries == null
            ) {
                return false
            }

            return true
        }

        fun setApiKey(key: String): RequestBuilder {
            apiKey = key
            return this
        }

        fun setPayloadType(type: PayloadType): RequestBuilder {
            payloadType = type
            return this
        }

        fun setPayload(jsonContent: String): RequestBuilder {
            payload = jsonContent
            return this
        }

        fun setBackoffRatio(value: Long): RequestBuilder {
            backoffDelay = value
            return this
        }

        fun setNumRetries(value: Int): RequestBuilder {
            numRetries = value
            return this
        }

        private fun buildWorkConstraint(): Constraints {
            return Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        }

        @Throws(Exception::class)
        fun build(): WorkRequest {
            if (!gotValidSpec()) {
                throw Exception("Provided values are not valid for upload work")
            }

            return OneTimeWorkRequestBuilder<UploadWork>()
                .setConstraints(buildWorkConstraint())
                .addTag(TAG)
                .setInputData(
                    buildInputData(
                        numRetries ?: throw Exception("numRetries must not be null"),
                        apiKey ?: throw Exception("apiKey must not be null"),
                        payloadType ?: throw Exception("payloadType must not be null"),
                        payload ?: throw Exception("payload must not be null"),
                    )
                )
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    backoffDelay ?: throw Exception("backoffDelay must not be null"),
                    TimeUnit.SECONDS
                )
                .build()
        }
    }
}
