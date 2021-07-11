package com.datartproject.dataart

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.datartproject.dataart.client.Action
import com.datartproject.dataart.client.ActionsQueue
import com.datartproject.dataart.client.DataArt
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class DataArtClientTest {
    @Test
    fun pushOrDrainActionsQueue() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val queue = ActionsQueue(appContext)
        queue.push(Action("some-key", "some-user-key", true, Date(), mapOf()))
        queue.drain(1)
    }

    @Test
    fun initializeDataArtClient() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val config = DataArt.Config("some-api-key")
        val dataart = DataArt(appContext, config)
    }

}
