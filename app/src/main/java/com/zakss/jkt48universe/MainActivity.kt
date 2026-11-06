
package com.zakss.jkt48universe

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.graphics.Color

import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@UnstableApi
class MainActivity : Activity() {

    private lateinit var root: FrameLayout
    private lateinit var playerView: PlayerView
    private lateinit var progressBar: ProgressBar

    private var player: ExoPlayer? = null
    private var introFinished = false

    private val handler = Handler(Looper.getMainLooper())

    private val progressUpdater = object : Runnable {
        override fun run() {
            val currentPlayer = player

            if (!introFinished && currentPlayer != null) {
                val duration = currentPlayer.duration
                if (duration > 0) {
                    progressBar.max = duration.toInt()
                    progressBar.progress =
                        currentPlayer.currentPosition.toInt()
                }

                handler.postDelayed(this, 200)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showIntro()
    }

    private fun showIntro() {
        root = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
        }

        playerView = PlayerView(this).apply {
            useController = false
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            keepScreenOn = true
        }

        root.addView(
            playerView,
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
            val exoPlayer = ExoPlayer.Builder(this).build()
            player = exoPlayer
            playerView.player = exoPlayer

            val videoUri = Uri.parse(
                "android.resource://$packageName/${R.raw.intro}"
            )

            exoPlayer.addListener(object : Player.Listener {

                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) {
                        val duration = exoPlayer.duration
                        if (duration > 0) {
                            progressBar.max = duration.toInt()
                        }

                        handler.removeCallbacks(progressUpdater)
                        handler.post(progressUpdater)
                    }

                    if (state == Player.STATE_ENDED) {
                        openHome()
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    error.printStackTrace()

                    Toast.makeText(
                        this@MainActivity,
                        "Video intro gagal diputar",
                        Toast.LENGTH_LONG
                    ).show()

                    openHome()
                }
            })

            exoPlayer.setMediaItem(MediaItem.fromUri(videoUri))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true

        } catch (e: Exception) {
            e.printStackTrace()

            Toast.makeText(
                this,
                "Gagal membuka video intro",
                Toast.LENGTH_LONG
            ).show()

            openHome()
        }
    }

    private fun releasePlayer() {
        handler.removeCallbacks(progressUpdater)

        playerView.player = null
        player?.release()
        player = null
    }

    private fun openHome() {
        if (introFinished) return

        introFinished = true
        releasePlayer()
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

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onDestroy() {
        releasePlayer()
        super.onDestroy()
    }
}
