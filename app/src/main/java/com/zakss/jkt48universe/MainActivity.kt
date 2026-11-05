package com.zakss.jkt48universe

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.VideoView
import android.widget.MediaController
import android.widget.Toast
import android.graphics.Color
import android.net.Uri

class MainActivity : Activity() {

    private lateinit var root: FrameLayout
    private lateinit var videoView: VideoView
    private lateinit var progressBar: ProgressBar
    private var introFinished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showIntro()
    }

    private fun showIntro() {
        root = FrameLayout(this)
        root.setBackgroundColor(Color.BLACK)

        videoView = VideoView(this)

        val videoParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        root.addView(videoView, videoParams)

        progressBar = ProgressBar(this, null,
            android.R.attr.progressBarStyleHorizontal)

        val progressParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            8
        )

        progressParams.gravity = Gravity.BOTTOM
        progressParams.setMargins(32, 0, 32, 36)

        root.addView(progressBar, progressParams)

        setContentView(root)

        try {
            val videoUri = Uri.parse(
                "android.resource://$packageName/${R.raw.intro}"
            )

            videoView.setVideoURI(videoUri)

            videoView.setOnPreparedListener { mediaPlayer ->
                mediaPlayer.isLooping = false
                videoView.start()
            }

            videoView.setOnCompletionListener {
                openHome()
            }

            videoView.setOnErrorListener { _, what, extra ->
                Toast.makeText(
                    this,
                    "Video intro gagal diputar ($what, $extra)",
                    Toast.LENGTH_LONG
                ).show()

                openHome()
                true
            }

        } catch (e: Exception) {
            openHome()
        }
    }

    private fun openHome() {
        if (introFinished) return
        introFinished = true

        showHome()
    }

    private fun showHome() {
        val home = FrameLayout(this)
        home.setBackgroundColor(Color.rgb(255, 235, 245))

        val title = TextView(this).apply {
            text = "JKT48 Universe"
            textSize = 28f
            setTextColor(Color.rgb(210, 60, 130))
            gravity = Gravity.CENTER
        }

        home.addView(
            title,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        setContentView(home)
    }
}

