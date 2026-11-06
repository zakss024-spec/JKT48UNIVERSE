package com.zakss.jkt48universe

import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@UnstableApi
class MainActivity : ComponentActivity() {

    private var player: ExoPlayer? = null
    private lateinit var root: FrameLayout

    private val pink = Color.rgb(238, 91, 158)
    private val dark = Color.rgb(35, 25, 42)
    private val light = Color.rgb(255, 244, 250)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = dark
        window.navigationBarColor = dark
        window.decorView.systemUiVisibility = 0

        root = FrameLayout(this)
        setContentView(root)

        showIntro()
    }

    private fun showIntro() {
        root.removeAllViews()

        val layout = FrameLayout(this)
        layout.setBackgroundColor(Color.BLACK)
        root.addView(layout)

        val videoView = PlayerView(this).apply {
            useController = false
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            keepScreenOn = true
        }

        layout.addView(
            videoView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        val progressParams = FrameLayout.LayoutParams(
            180,
            5,
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        ).apply {
            bottomMargin = 35
        }
        layout.addView(progress, progressParams)

        val exoPlayer = ExoPlayer.Builder(this).build()
        player = exoPlayer
        videoView.player = exoPlayer

        exoPlayer.setMediaItem(
            MediaItem.fromUri(
                Uri.parse("android.resource://$packageName/${R.raw.intro}")
            )
        )

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    releasePlayer()
                    showHome()
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Toast.makeText(
                    this@MainActivity,
                    "Video intro gagal: ${error.errorCodeName}",
                    Toast.LENGTH_LONG
                ).show()

                releasePlayer()
                showHome()
            }
        })

        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }

    private fun showHome() {
        root.removeAllViews()
        root.setBackgroundColor(light)

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(light)
        }

        root.addView(
            mainLayout,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // HEADER
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 22, 20, 18)
            setBackgroundColor(pink)
        }

        val title = TextView(this).apply {
            text = "JKT48 Universe"
            textSize = 25f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }

        val subtitle = TextView(this).apply {
            text = "Your world of idols ✨"
            textSize = 13f
            setTextColor(Color.WHITE)
        }

        header.addView(title)
        header.addView(subtitle)

        mainLayout.addView(header)

        // CONTENT
        val content = FrameLayout(this).apply {
            setBackgroundColor(light)
        }

        mainLayout.addView(
            content,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        // BOTTOM NAVIGATION
        val bottomScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            setBackgroundColor(Color.WHITE)
            overScrollMode = View.OVER_SCROLL_NEVER
        }

        val bottomMenu = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(5, 8, 5, 8)
        }

        val menuItems = listOf(
            Triple("⌂", "Home", "Home"),
            Triple("♡", "Members", "Members"),
            Triple("★", "Events", "Events"),
            Triple("☏", "Chat", "Chat"),
            Triple("✦", "Quiz", "Quiz"),
            Triple("▣", "Gallery", "Gallery"),
            Triple("♫", "News", "News")
        )

        fun showPage(page: String) {
            content.removeAllViews()

            val pageLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.TOP
                setPadding(22, 28, 22, 20)
            }

            val pageTitle = TextView(this).apply {
                text = page
                textSize = 25f
                setTextColor(dark)
                typeface = Typeface.DEFAULT_BOLD
            }

            val description = TextView(this).apply {
                text = when (page) {
                    "Home" -> "Selamat datang di JKT48 Universe! ✨"
                    "Members" -> "Jelajahi halaman member JKT48."
                    "Events" -> "Informasi event dan jadwal."
                    "Chat" -> "Ruang chat virtual JKT48 Universe."
                    "Quiz" -> "Uji pengetahuanmu tentang JKT48!"
                    "Gallery" -> "Koleksi foto dan momen."
                    "News" -> "Berita dan informasi terbaru."
                    else -> ""
                }
                textSize = 15f
                setTextColor(Color.DKGRAY)
                setPadding(0, 12, 0, 0)
            }

            pageLayout.addView(pageTitle)
            pageLayout.addView(description)

            content.addView(
                pageLayout,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )

            // Home welcome card
            if (page == "Home") {
                val card = TextView(this).apply {
                    text = "🌸  Welcome to JKT48 Universe\n\nNikmati dunia JKT48 dalam satu aplikasi."
                    textSize = 17f
                    setTextColor(dark)
                    setPadding(18, 22, 18, 22)
                    setBackgroundColor(Color.WHITE)
                }

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 28
                }

                pageLayout.addView(card, params)
            }
        }

        menuItems.forEach { item ->
            val (icon, label, page) = item

            val menuButton = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(12, 5, 12, 5)
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    showPage(page)
                    Toast.makeText(
                        this@MainActivity,
                        page,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            val iconText = TextView(this).apply {
                text = icon
                textSize = 23f
                setTextColor(pink)
                gravity = Gravity.CENTER
            }

            val labelText = TextView(this).apply {
                text = label
                textSize = 11f
                setTextColor(dark)
                gravity = Gravity.CENTER
            }

            menuButton.addView(iconText)
            menuButton.addView(labelText)

            bottomMenu.addView(
                menuButton,
                LinearLayout.LayoutParams(
                    78,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }

        bottomScroll.addView(bottomMenu)

        mainLayout.addView(
            bottomScroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                65
            )
        )

        showPage("Home")
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

