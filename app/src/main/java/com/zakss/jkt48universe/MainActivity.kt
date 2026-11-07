
package com.zakss.jkt48universe

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
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
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@UnstableApi
class MainActivity : Activity() {

    private var player: ExoPlayer? = null
    private lateinit var root: FrameLayout

    private val pink = Color.rgb(238, 91, 158)
    private val dark = Color.rgb(35, 25, 42)
    private val light = Color.rgb(255, 244, 250)
    private val palePink = Color.rgb(255, 225, 240)

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

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

        val progress = ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        )

        val progressParams = FrameLayout.LayoutParams(
            dp(180),
            dp(5),
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        ).apply {
            bottomMargin = dp(35)
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

            override fun onPlayerError(
                error: androidx.media3.common.PlaybackException
            ) {
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

    private fun makeBackground(
        color: Int,
        radius: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
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
            setPadding(dp(22), dp(22), dp(22), dp(20))
            setBackgroundColor(pink)
        }

        val title = TextView(this).apply {
            text = "JKT48 Universe"
            textSize = 26f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }

        val subtitle = TextView(this).apply {
            text = "Your world of idols ✨"
            textSize = 14f
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
        val navContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            setPadding(0, dp(5), 0, dp(4))
        }

        val bottomScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
        }

        val bottomMenu = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(5), dp(3), dp(5), dp(3))
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

        val navButtons = mutableMapOf<String, LinearLayout>()

        fun showPage(page: String) {
            content.removeAllViews()

            val pageLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.TOP
                setPadding(dp(22), dp(28), dp(22), dp(20))
            }

            val pageTitle = TextView(this).apply {
                text = page
                textSize = 26f
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

                textSize = 16f
                setTextColor(Color.DKGRAY)
                setPadding(0, dp(12), 0, 0)
            }

            pageLayout.addView(pageTitle)
            pageLayout.addView(description)

            if (page == "Home") {
                val card = TextView(this).apply {
                    text = "🌸  Welcome to JKT48 Universe\n\nNikmati dunia JKT48 dalam satu aplikasi."
                    textSize = 17f
                    setTextColor(dark)
                    setPadding(dp(18), dp(22), dp(18), dp(22))
                    background = makeBackground(Color.WHITE, 18)
                }

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(28)
                }

                pageLayout.addView(card, params)
            }

            content.addView(
                pageLayout,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )

            // ACTIVE NAVIGATION STYLE
            navButtons.forEach { (name, button) ->
                val active = name == page

                button.background = makeBackground(
                    if (active) palePink else Color.TRANSPARENT,
                    14
                )

                val icon = button.getChildAt(0) as TextView
                val label = button.getChildAt(1) as TextView
                val indicator = button.getChildAt(2)

                icon.setTextColor(if (active) pink else dark)
                label.setTextColor(if (active) pink else Color.DKGRAY)

                label.typeface = if (active) {
                    Typeface.DEFAULT_BOLD
                } else {
                    Typeface.DEFAULT
                }

                indicator.visibility =
                    if (active) View.VISIBLE else View.INVISIBLE
            }
        }

        // CREATE MENU BUTTONS
        menuItems.forEach { item ->
            val (icon, label, page) = item

            val menuButton = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(5), dp(7), dp(5), dp(5))
                isClickable = true
                isFocusable = true
                background = makeBackground(Color.TRANSPARENT, 14)

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
                textSize = 29f
                setTextColor(dark)
                gravity = Gravity.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }

            val labelText = TextView(this).apply {
                text = label
                textSize = 12f
                setTextColor(Color.DKGRAY)
                gravity = Gravity.CENTER
                maxLines = 1
            }

            val indicator = View(this).apply {
                background = makeBackground(pink, 10)
                visibility = View.INVISIBLE
            }

            menuButton.addView(
                iconText,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(36)
                )
            )

            menuButton.addView(
                labelText,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(22)
                )
            )

            menuButton.addView(
                indicator,
                LinearLayout.LayoutParams(
                    dp(30),
                    dp(3)
                ).apply {
                    topMargin = dp(4)
                }
            )

            bottomMenu.addView(
                menuButton,
                LinearLayout.LayoutParams(
                    dp(78),
                    dp(76)
                )
            )

            navButtons[page] = menuButton
        }

        bottomScroll.addView(bottomMenu)

        navContainer.addView(
            bottomScroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(82)
            )
        )

        mainLayout.addView(
            navContainer,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(88)
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
