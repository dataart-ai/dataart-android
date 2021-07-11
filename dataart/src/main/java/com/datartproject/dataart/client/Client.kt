package com.datartproject.dataart.client

import android.content.Context
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkRequest
import java.util.*

interface DataArtClient {
    fun emitAction(key: String,
                   userKey: String,
                   isAnonymousUser: Boolean,
                   timestamp: Date,
                   metadata: Map<String, String>)

    fun identify(userKey: String, metadata: Map<String, String>)
}

class DataArt(appContext: Context, config: Config): DataArtClient {
    data class Config(
        /**
         * The authorization key for sending requests. You can find this value
         * in your dashboard. Contact support if you need help.
         */
        val apiKey: String,
        /**
         * The number of times each request is tried before giving up.
         */
        val flushNumRetries: Int = DEFAULT_FLUSH_NUM_RETRIES,
        /**
         * The constant time (seconds) added for each execution retry. For instance
         * a flushNumRetries of 3 and flushBackoffRatio of 10 will cause in 10, 20, 30 seconds
         * of delay before giving up.
         */
        val flushBackoffRatio: Long = DEFAULT_FLUSH_BACKOFF_RATIO,
        /**
         * The number of action events in batch request. If you emit
         * this much actions, a request will be created and sent.
         */
        val flushActionsBatchSize: Int = DEFAULT_FLUSH_ACTIONS_BATCH_SIZE,
        /**
         * The timer duration (milliseconds) for flushing actions. If this much time is passed
         * and there's some actions left, they will be sent to server.
         */
        val flushInterval: Long = DEFAULT_FLUSH_INTERVAL) {

        companion object {
            private const val DEFAULT_FLUSH_NUM_RETRIES = 3
            private const val DEFAULT_FLUSH_BACKOFF_RATIO = WorkRequest.MIN_BACKOFF_MILLIS
            private const val DEFAULT_FLUSH_ACTIONS_BATCH_SIZE = 20
            private const val DEFAULT_FLUSH_INTERVAL = PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS
        }

        init {
            require(apiKey.isNotEmpty()) {
                "apiKey must not be empty"
            }

            require(flushNumRetries >= DEFAULT_FLUSH_NUM_RETRIES) {
                "flushNumRetries must be at least 3"
            }

            require(flushBackoffRatio >= DEFAULT_FLUSH_BACKOFF_RATIO) {
                "flushBackoffRatio must greater than 10"
            }

            require(flushActionsBatchSize >= DEFAULT_FLUSH_ACTIONS_BATCH_SIZE) {
                "flushActionsBatchSize can't be less than 20"
            }

            require(flushInterval >= DEFAULT_FLUSH_INTERVAL) {
                "flushInterval must be at least PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS"
            }
        }
    }

    private val uploader = Uploader(appContext, config.apiKey, config.flushActionsBatchSize,
        config.flushBackoffRatio, config.flushNumRetries, config.flushInterval)

    init {
        uploader.startPeriodicFlush()
    }

    override fun emitAction(
        key: String,
        userKey: String,
        isAnonymousUser: Boolean,
        timestamp: Date,
        metadata: Map<String, String>
    ) {
        uploader.uploadAction(Action(key, userKey, isAnonymousUser, timestamp, metadata))
    }

    override fun identify(userKey: String, metadata: Map<String, String>) {
        uploader.uploadIdentity(Identity(userKey, metadata))
    }
}
