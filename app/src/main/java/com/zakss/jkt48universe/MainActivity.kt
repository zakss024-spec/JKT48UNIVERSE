
package com.zakss.jkt48universe

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.VideoView
import android.widget.Toast
import android.graphics.Color
import java.io.File

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
        root = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
        }

        videoView = VideoView(this)

        root.addView(
            videoView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        progressBar = ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        )

        val progressParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            8
        ).apply {
            gravity = Gravity.BOTTOM
            setMargins(32, 0, 32, 36)
        }

        root.addView(progressBar, progressParams)
        setContentView(root)

        try {
            val videoFile = File(cacheDir, "intro.mp4")

            resources.openRawResource(R.raw.intro).use { input ->
                videoFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            videoView.setVideoPath(videoFile.absolutePath)

            videoView.setOnPreparedListener { player ->
                player.isLooping = false
                progressBar.max = player.duration
                videoView.start()

                val progressUpdater = object : Runnable {
                    override fun run() {
                        if (!introFinished && videoView.isPlaying) {
                            progressBar.progress = videoView.currentPosition
                            progressBar.postDelayed(this, 200)
                        }
                    }
                }

                videoView.post(progressUpdater)
            }

            videoView.setOnCompletionListener {
                openHome()
            }

            videoView.setOnErrorListener { _, what, extra ->
                Toast.makeText(
                    this,
                    "Video gagal: $what, $extra",
                    Toast.LENGTH_LONG
                ).show()

                openHome()
                true
            }

        } catch (e: Exception) {
            e.printStackTrace()

            Toast.makeText(
                this,
                "File intro tidak dapat dibaca",
                Toast.LENGTH_LONG
            ).show()

            openHome()
        }
    }

    private fun openHome() {
        if (introFinished) return
        introFinished = true
        showHome()
    }

    private fun showHome() {
        val home = FrameLayout(this).apply {
            setBackgroundColor(Color.rgb(255, 235, 245))
        }

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
