package com.datartproject.dataart_sample_phone

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.datartproject.dataart.client.DataArt
import java.util.Date

class MainActivity : AppCompatActivity() {
    private lateinit var dataart: DataArt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dataart = DataArt(this, DataArt.Config("your-api-key"))
    }

    fun onButtonClicked(v: View) {
        if (v.id == R.id.btn) {
            dataart.emitAction(
                "some-event-key",
                "some-user-key",
                false,
                Date(),
                mapOf()
            )
        }
    }
}
