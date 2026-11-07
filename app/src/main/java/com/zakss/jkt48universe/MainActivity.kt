
package com.zakss.jkt48universe

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
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

    private val handler = Handler(Looper.getMainLooper())
    private var progressRunnable: Runnable? = null
    private var introFinished = false

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

    // ==========================================
    // INTRO VIDEO
    // ==========================================

    private fun showIntro() {
        introFinished = false
        root.removeAllViews()

        // FULLSCREEN
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        val layout = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
        }

        root.addView(
            layout,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // VIDEO VIEW
        val videoView = PlayerView(this).apply {
            useController = false

            // Fill screen without black letterbox.
            // Some edges may be cropped to preserve aspect ratio.
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM

            keepScreenOn = true
        }

        layout.addView(
            videoView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // PROCESS PANEL
        val processPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(
                dp(22),
                dp(14),
                dp(22),
                dp(14)
            )

            background = makeBackground(
                Color.argb(190, 25, 15, 30),
                22
            )
        }

        // PROCESS TEXT
        val processText = TextView(this).apply {
            text = "Process"
            textSize = 14f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        processPanel.addView(
            processText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(24)
            )
        )

        // PROGRESS BAR
        val progress = ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            max = 1000
            progress = 0

            progressTintList =
                android.content.res.ColorStateList.valueOf(pink)

            progressBackgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    Color.rgb(110, 75, 95)
                )
        }

        processPanel.addView(
            progress,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(5)
            ).apply {
                topMargin = dp(12)
                bottomMargin = dp(14)
            }
        )

        // LIQUID GLASS BACKGROUND
        val skipGlass = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(
                Color.argb(105, 255, 255, 255),
                Color.argb(35, 255, 255, 255),
                Color.argb(85, 238, 91, 158)
            )
        ).apply {
            cornerRadius = dp(30).toFloat()

            setStroke(
                dp(1),
                Color.argb(180, 255, 255, 255)
            )
        }

        // SKIP BUTTON
        val skipButton = TextView(this).apply {
            text = "SKIP  ›"
            textSize = 13f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER

            setPadding(
                dp(24),
                dp(9),
                dp(24),
                dp(9)
            )

            background = skipGlass
            elevation = dp(6).toFloat()

            isClickable = true
            isFocusable = true

            // TOUCH ANIMATION
            setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        view.animate()
                            .scaleX(0.94f)
                            .scaleY(0.94f)
                            .alpha(0.8f)
                            .setDuration(100)
                            .start()
                    }

                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .setDuration(150)
                            .start()
                    }
                }

                false
            }

            setOnClickListener {
                finishIntro()
            }
        }

        processPanel.addView(
            skipButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // PANEL AT BOTTOM CENTER
        val panelParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        ).apply {
            setMargins(
                dp(20),
                0,
                dp(20),
                dp(35)
            )
        }

        layout.addView(processPanel, panelParams)

        // EXOPLAYER
        val exoPlayer = ExoPlayer.Builder(this).build()

        player = exoPlayer
        videoView.player = exoPlayer

        exoPlayer.setMediaItem(
            MediaItem.fromUri(
                Uri.parse(
                    "android.resource://$packageName/${R.raw.intro}"
                )
            )
        )

        exoPlayer.addListener(object : Player.Listener {

            override fun onPlaybackStateChanged(state: Int) {

                if (state == Player.STATE_READY) {
                    startProgressUpdater(progress)
                }

                if (state == Player.STATE_ENDED) {
                    finishIntro()
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

                finishIntro()
            }
        })

        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    // ==========================================
    // PROGRESS UPDATER
    // ==========================================

    private fun startProgressUpdater(progress: ProgressBar) {
        stopProgressUpdater()

        val runnable = object : Runnable {

            override fun run() {
                val currentPlayer = player ?: return

                val duration = currentPlayer.duration
                val position = currentPlayer.currentPosition

                if (duration > 0) {
                    progress.progress = (
                        position.toDouble() /
                            duration.toDouble() * 1000
                    ).toInt().coerceIn(0, 1000)
                }

                if (!introFinished) {
                    handler.postDelayed(this, 100)
                }
            }
        }

        progressRunnable = runnable
        handler.post(runnable)
    }

    private fun stopProgressUpdater() {
        progressRunnable?.let {
            handler.removeCallbacks(it)
        }

        progressRunnable = null
    }

    // ==========================================
    // FINISH INTRO
    // ==========================================

    private fun finishIntro() {
        if (introFinished) return

        introFinished = true

        stopProgressUpdater()
        releasePlayer()

        showHome()
    }

    private fun releasePlayer() {
        stopProgressUpdater()

        player?.release()
        player = null
    }

    // ==========================================
    // BACKGROUND DESIGN
    // ==========================================

    private fun makeBackground(
        color: Int,
        radius: Int
    ): GradientDrawable {

        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
    }

    // ==========================================
    // HOME PAGE
    // ==========================================

    private fun showHome() {

        root.removeAllViews()

        // RESTORE SYSTEM BARS
        window.decorView.systemUiVisibility = 0
        window.statusBarColor = dark
        window.navigationBarColor = dark

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

            setPadding(
                dp(22),
                dp(22),
                dp(22),
                dp(20)
            )

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

        // BOTTOM NAVIGATION CONTAINER
        val navContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)

            setPadding(
                0,
                dp(5),
                0,
                dp(4)
            )
        }

        val bottomScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
        }

        val bottomMenu = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            setPadding(
                dp(5),
                dp(3),
                dp(5),
                dp(3)
            )
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

        val navButtons =
            mutableMapOf<String, LinearLayout>()

        // PAGE NAVIGATION
        fun showPage(page: String) {

            content.removeAllViews()

            val pageLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.TOP

                setPadding(
                    dp(22),
                    dp(28),
                    dp(22),
                    dp(20)
                )
            }

            val pageTitle = TextView(this).apply {
                text = page
                textSize = 26f
                setTextColor(dark)
                typeface = Typeface.DEFAULT_BOLD
            }

            val description = TextView(this).apply {

                text = when (page) {
                    "Home" ->
                        "Selamat datang di JKT48 Universe! ✨"

                    "Members" ->
                        "Jelajahi halaman member JKT48."

                    "Events" ->
                        "Informasi event dan jadwal."

                    "Chat" ->
                        "Ruang chat virtual JKT48 Universe."

                    "Quiz" ->
                        "Uji pengetahuanmu tentang JKT48!"

                    "Gallery" ->
                        "Koleksi foto dan momen."

                    "News" ->
                        "Berita dan informasi terbaru."

                    else -> ""
                }

                textSize = 16f
                setTextColor(Color.DKGRAY)

                setPadding(
                    0,
                    dp(12),
                    0,
                    0
                )
            }

            pageLayout.addView(pageTitle)
            pageLayout.addView(description)

            // HOME CARD
            if (page == "Home") {

                val card = TextView(this).apply {
                    text =
                        "🌸  Welcome to JKT48 Universe\n\n" +
                        "Nikmati dunia JKT48 dalam satu aplikasi."

                    textSize = 17f
                    setTextColor(dark)

                    setPadding(
                        dp(18),
                        dp(22),
                        dp(18),
                        dp(22)
                    )

                    background = makeBackground(
                        Color.WHITE,
                        18
                    )
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

                val icon =
                    button.getChildAt(0) as TextView

                val label =
                    button.getChildAt(1) as TextView

                val indicator =
                    button.getChildAt(2)

                icon.setTextColor(
                    if (active) pink else dark
                )

                label.setTextColor(
                    if (active) pink else Color.DKGRAY
                )

                label.typeface = if (active) {
                    Typeface.DEFAULT_BOLD
                } else {
                    Typeface.DEFAULT
                }

                indicator.visibility =
                    if (active) {
                        View.VISIBLE
                    } else {
                        View.INVISIBLE
                    }
            }
        }

        // CREATE MENU BUTTONS
        menuItems.forEach { item ->

            val (icon, label, page) = item

            val menuButton = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER

                setPadding(
                    dp(5),
                    dp(7),
                    dp(5),
                    dp(5)
                )

                isClickable = true
                isFocusable = true

                background = makeBackground(
                    Color.TRANSPARENT,
                    14
                )

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

    // ==========================================
    // ACTIVITY LIFECYCLE
    // ==========================================

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onDestroy() {
        releasePlayer()
        super.onDestroy()
    }
}
