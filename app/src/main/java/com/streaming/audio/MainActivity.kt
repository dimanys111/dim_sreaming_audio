package com.streaming.audio

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.streaming.streaming_audio_library.StreamingClass

class MainActivity : AppCompatActivity() {

    private var m_textView: EditText? = null
    private var m_floatingActionButton: FloatingActionButton? = null
    private var m_streamingClass : StreamingClass? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        m_streamingClass = StreamingClass(this, this::updateResults, this::changState)
        m_textView = findViewById(R.id.textView)
        m_floatingActionButton = findViewById(R.id.fab)
        m_floatingActionButton?.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
        m_floatingActionButton?.setOnClickListener {
            m_streamingClass?.changStreaming()
        }
    }

    private fun updateResults(string: String) {
        m_textView?.setText(string)
    }

    private fun changState(state: Boolean) {
        if (state) {
            m_floatingActionButton?.backgroundTintList = ColorStateList.valueOf(Color.RED)
            m_textView?.setText("")
            Toast.makeText(applicationContext, "Старт стриминга", Toast.LENGTH_SHORT).show()
        } else {
            m_floatingActionButton?.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
            Toast.makeText(applicationContext, "Стоп стриминга", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        m_streamingClass?.onRequestPermissionsResult(requestCode, grantResults)
    }
}