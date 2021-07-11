package com.datartproject.dataart

import com.datartproject.dataart.client.formatAsAtom
import org.junit.Test
import java.util.*

class DateUnitTest {
    @Test
    fun date_format() {
        val d = Date()
        d.formatAsAtom()
    }
}