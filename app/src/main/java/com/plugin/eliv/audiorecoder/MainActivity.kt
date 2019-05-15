package com.plugin.eliv.audiorecoder

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.plugin.eliv.recoderlibrary.AudioEngine
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AudioEngine.getInstance().init(this).enableSystemAEC()

        recode.setOnClickListener {
            AudioEngine.getInstance().startRecord("[path]")
        }

        stopRecode.setOnClickListener {
            AudioEngine.getInstance().stopRecord()
        }

        play.setOnClickListener {
            AudioEngine.getInstance().startPlay("[path]")
        }

        stopPlay.setOnClickListener {
            AudioEngine.getInstance().stopPlay()
        }
    }
}
