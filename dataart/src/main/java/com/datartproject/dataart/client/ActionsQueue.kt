package com.datartproject.dataart.client

import android.content.Context
import com.squareup.tape2.ObjectQueue
import com.squareup.tape2.ObjectQueue.Converter
import com.squareup.tape2.QueueFile
import kotlinx.serialization.json.Json
import java.io.File
import java.io.OutputStream

class ActionsQueue(appContext: Context) {
    companion object {
        private val opLock = Object()

        private const val STORAGE_FILE_NAME = "actions.queue"
    }

    private val queue: ObjectQueue<Action>

    init {
        val storage = File(appContext.filesDir, STORAGE_FILE_NAME)
        val queueFile = QueueFile.Builder(storage).build()
        val converter = ActionJsonConverter()
        queue = ObjectQueue.create(queueFile, converter)
    }

    fun size(): Int {
        synchronized(opLock) {
            return@size queue.size()
        }
    }

    fun push(item: Action) {
        synchronized(opLock) {
            queue.add(item)
        }
    }

    fun drain(count: Int): List<Action> {
        synchronized(opLock) {
            val actions = queue.peek(count)
            queue.remove(count)
            return@drain actions.toList()
        }
    }
}

class ActionJsonConverter: Converter<Action> {
    override fun from(source: ByteArray): Action {
        return Json.decodeFromString(Action.serializer(), String(source))
    }

    override fun toStream(value: Action, os: OutputStream) {
        os.buffered().use { sink ->
            sink.write(Json.encodeToString(Action.serializer(), value).toByteArray())
        }
    }
}
