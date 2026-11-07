
package com.zakss.jkt48universe

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
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

        window.decorView.systemUiVisibility = 0
        window.statusBarColor = dark
        window.navigationBarColor = dark

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

        val processPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(22), dp(12), dp(22), dp(12))
            background = makeBackground(
                Color.argb(190, 25, 15, 30), 22
            )
        }

        val processRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val processText = TextView(this).apply {
            text = "Process"
            textSize = 14f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }

        val skipGlass = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(
                Color.argb(105, 255, 255, 255),
                Color.argb(35, 255, 255, 255),
                Color.argb(85, 238, 91, 158)
            )
        ).apply {
            cornerRadius = dp(30).toFloat()
            setStroke(dp(1), Color.argb(180, 255, 255, 255))
        }

        val skipButton = TextView(this).apply {
            text = "SKIP  ›"
            textSize = 13f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(9), dp(18), dp(9))
            background = skipGlass
            elevation = dp(6).toFloat()

            isClickable = true
            isFocusable = true

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

        processRow.addView(
            processText,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        processRow.addView(skipButton)
        processPanel.addView(processRow)

        val progress = ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            max = 1000
            this.progress = 0
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
            }
        )

        val panelParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        ).apply {
            setMargins(dp(20), 0, dp(20), dp(35))
        }

        layout.addView(processPanel, panelParams)

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
    // DESIGN HELPERS
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

    private fun animateIn(view: View) {
        view.alpha = 0f
        view.translationY = dp(12).toFloat()
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(230)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun makeText(
        text: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }
    }

    // ==========================================
    // HOME PAGE
    // ==========================================

    private fun showHome() {
        root.removeAllViews()

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

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(20), dp(22), dp(18))
            setBackgroundColor(pink)
        }

        header.addView(
            makeText("JKT48 Universe 🌸", 25f, Color.WHITE, true)
        )

        header.addView(
            makeText("Your world of idols ✨", 14f, Color.WHITE)
        )

        mainLayout.addView(header)

        val content = FrameLayout(this)

        mainLayout.addView(
            content,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

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

        fun addSectionTitle(parent: LinearLayout, text: String) {
            val label = makeText(text, 19f, dark, true)

            parent.addView(
                label,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(22)
                    bottomMargin = dp(10)
                }
            )
        }

        fun addInfoCard(
            parent: LinearLayout,
            heading: String,
            description: String,
            symbol: String
        ) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(17), dp(16), dp(17), dp(16))
                background = makeBackground(Color.WHITE, 18)
            }

            card.addView(makeText(symbol, 25f, pink))
            card.addView(makeText(heading, 16f, dark, true))

            val detail = makeText(description, 13f, Color.DKGRAY)
            detail.setPadding(0, dp(5), 0, 0)
            card.addView(detail)

            parent.addView(
                card,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dp(10)
                }
            )
        }

        fun showPage(page: String) {
            content.removeAllViews()

            val scroll = ScrollView(this).apply {
                isFillViewport = true
                setBackgroundColor(light)
            }

            val pageLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(20), dp(22), dp(20), dp(25))
            }

            pageLayout.addView(
                makeText(page, 25f, dark, true)
            )

            if (page == "Home") {

                val banner = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(20), dp(22), dp(20), dp(22))
                    background = GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        intArrayOf(
                            Color.rgb(238, 91, 158),
                            Color.rgb(177, 105, 190)
                        )
                    ).apply {
                        cornerRadius = dp(22).toFloat()
                    }
                }

                banner.addView(
                    makeText(
                        "Welcome to JKT48 Universe!",
                        20f,
                        Color.WHITE,
                        true
                    )
                )

                val bannerDescription = makeText(
                    "Satu tempat untuk menjelajahi dunia JKT48.",
                    14f,
                    Color.WHITE
                )
                bannerDescription.setPadding(0, dp(8), 0, 0)
                banner.addView(bannerDescription)

                pageLayout.addView(
                    banner,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = dp(18)
                    }
                )

                addSectionTitle(pageLayout, "♡ Member Spotlight")

                addInfoCard(
                    pageLayout,
                    "Member pilihan",
                    "Bagian profil member akan segera hadir.",
                    "🌸"
                )

                addSectionTitle(pageLayout, "✦ Jelajahi Menu")

                val quickMenu = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER
                }

                val quickItems = listOf(
                    Triple("♡", "Members", "Members"),
                    Triple("★", "Events", "Events"),
                    Triple("✦", "Quiz", "Quiz")
                )

                quickItems.forEach { item ->
                    val (icon, label, destination) = item

                    val quickCard = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        gravity = Gravity.CENTER
                        setPadding(dp(8), dp(15), dp(8), dp(15))
                        background = makeBackground(Color.WHITE, 16)
                        isClickable = true
                        isFocusable = true

                        setOnClickListener {
                            showPage(destination)
                        }
                    }

                    quickCard.addView(
                        makeText(icon, 25f, pink).apply {
                            gravity = Gravity.CENTER
                        }
                    )

                    quickCard.addView(
                        makeText(label, 12f, dark, true).apply {
                            gravity = Gravity.CENTER
                        }
                    )

                    quickMenu.addView(
                        quickCard,
                        LinearLayout.LayoutParams(
                            0,
                            dp(85),
                            1f
                        ).apply {
                            marginEnd = dp(6)
                            marginStart = dp(6)
                        }
                    )
                }

                pageLayout.addView(quickMenu)

                addSectionTitle(pageLayout, "★ Events")

                addInfoCard(
                    pageLayout,
                    "Event JKT48",
                    "Informasi jadwal dan event akan ditampilkan di sini.",
                    "🎤"
                )

                addSectionTitle(pageLayout, "♫ Update Terbaru")

                addInfoCard(
                    pageLayout,
                    "Universe Update",
                    "Bagian berita terbaru sedang disiapkan.",
                    "📰"
                )

            } else if (page == "Members") {

                showMembersDirectory(pageLayout)

            } else {

                val description = when (page) {
                    "Events" -> "Temukan informasi event dan jadwal."
                    "Chat" -> "Ruang interaksi komunitas JKT48 Universe."
                    "Quiz" -> "Uji pengetahuanmu tentang JKT48!"
                    "Gallery" -> "Koleksi foto dan berbagai momen."
                    "News" -> "Informasi dan berita terbaru."
                    else -> ""
                }

                val info = makeText(description, 15f, Color.DKGRAY)
                info.setPadding(0, dp(12), 0, dp(18))
                pageLayout.addView(info)

                addInfoCard(
                    pageLayout,
                    "Segera hadir",
                    "Fitur halaman $page sedang dalam tahap pengembangan.",
                    "🌸"
                )
            }

            scroll.addView(pageLayout)

            content.addView(
                scroll,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )

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

        menuItems.forEach { item ->
            val (icon, label, page) = item

            val menuButton = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(5), dp(7), dp(5), dp(5))
                background = makeBackground(Color.TRANSPARENT, 14)
                isClickable = true
                isFocusable = true

                setOnClickListener {
                    showPage(page)
                }
            }

            val iconText = makeText(icon, 29f, dark, true).apply {
                gravity = Gravity.CENTER
            }

            val labelText = makeText(label, 12f, Color.DKGRAY).apply {
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
                LinearLayout.LayoutParams(dp(30), dp(3)).apply {
                    topMargin = dp(4)
                }
            )

            bottomMenu.addView(
                menuButton,
                LinearLayout.LayoutParams(dp(78), dp(76))
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
    // MEMBERS DIRECTORY - STAGE 1
    // ==========================================

    private fun showMembersDirectory(parent: LinearLayout) {

        var selectedCategory = "Aktif"
        var selectedGeneration = 0
        var searchQuery = ""

        val categories = listOf("Aktif", "Trainee", "Alumni")

        val intro = makeText(
            "Jelajahi member JKT48 berdasarkan status dan generasi.",
            14f,
            Color.DKGRAY
        )

        intro.setPadding(0, dp(10), 0, dp(16))
        parent.addView(intro)

        // SEARCH FIELD

        val searchBox = EditText(this).apply {
            hint = "Cari nama member..."
            textSize = 14f
            singleLine = true
            setTextColor(dark)
            setHintTextColor(Color.GRAY)
            setPadding(dp(15), dp(13), dp(15), dp(13))
            background = makeBackground(Color.WHITE, 15)
        }

        parent.addView(
            searchBox,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(50)
            ).apply {
                bottomMargin = dp(18)
            }
        )

        // CATEGORY FILTERS

        parent.addView(
            makeText("Kategori Member", 17f, dark, true)
        )

        val categoryRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        parent.addView(
            categoryRow,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(54)
            ).apply {
                topMargin = dp(8)
            }
        )

        // GENERATION FILTERS

        parent.addView(
            makeText("Filter Generasi", 17f, dark, true).apply {
                setPadding(0, dp(12), 0, dp(8))
            }
        )

        val generationScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
        }

        val generationRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        generationScroll.addView(generationRow)

        parent.addView(
            generationScroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(44)
            )
        )

        // RESULT AREA

        val resultArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        parent.addView(
            resultArea,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(18)
            }
        )

        fun makeFilterButton(
            label: String,
            active: Boolean
        ): TextView {
            return TextView(this).apply {
                text = label
                textSize = 13f
                gravity = Gravity.CENTER
                typeface = if (active) {
                    Typeface.DEFAULT_BOLD
                } else {
                    Typeface.DEFAULT
                }
                setTextColor(if (active) pink else dark)
                setPadding(dp(15), dp(9), dp(15), dp(9))
                background = makeBackground(
                    if (active) palePink else Color.WHITE,
                    30
                )
                isClickable = true
                isFocusable = true
            }
        }

        fun refreshResults() {
            resultArea.animate().cancel()
            resultArea.removeAllViews()

            val categoryDescription = when (selectedCategory) {
                "Aktif" -> "Member yang masih aktif."
                "Trainee" -> "Member yang berstatus trainee."
                else -> "Member yang telah lulus."
            }

            val countLabel = makeText(
                "$selectedCategory • ${if (selectedGeneration == 0) "Semua generasi" else "Generasi $selectedGeneration"}",
                15f,
                dark,
                true
            )

            resultArea.addView(countLabel)

            val detail = makeText(
                categoryDescription,
                12f,
                Color.DKGRAY
            )
            detail.setPadding(0, dp(5), 0, dp(12))
            resultArea.addView(detail)

            val emptyCard = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(20), dp(25), dp(20), dp(25))
                background = makeBackground(Color.WHITE, 18)
            }

            emptyCard.addView(
                makeText("🌸", 38f, pink).apply {
                    gravity = Gravity.CENTER
                }
            )

            emptyCard.addView(
                makeText(
                    "Data Member Sedang Disiapkan",
                    16f,
                    dark,
                    true
                ).apply {
                    gravity = Gravity.CENTER
                    setPadding(0, dp(10), 0, dp(5))
                }
            )

            val emptyDescription = when {
                searchQuery.isNotBlank() ->
                    "Pencarian \"$searchQuery\" belum menemukan data. Data member resmi akan ditambahkan pada tahap berikutnya."

                selectedGeneration != 0 ->
                    "Data member Generasi $selectedGeneration belum dimasukkan."

                else ->
                    "Daftar member dan foto resmi akan ditambahkan setelah data diverifikasi."
            }

            emptyCard.addView(
                makeText(
                    emptyDescription,
                    13f,
                    Color.DKGRAY
                ).apply {
                    gravity = Gravity.CENTER
                    setPadding(0, dp(5), 0, 0)
                }
            )

            resultArea.addView(
                emptyCard,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            animateIn(resultArea)
        }

        fun refreshGenerationButtons() {
            generationRow.removeAllViews()

            val generations = mutableListOf(0)
            generations.addAll((1..14).toList())

            generations.forEach { generation ->
                val label = if (generation == 0) {
                    "Semua"
                } else {
                    "Gen $generation"
                }

                val button = makeFilterButton(
                    label,
                    selectedGeneration == generation
                )

                button.setOnClickListener {
                    selectedGeneration = generation
                    refreshGenerationButtons()
                    refreshResults()
                }

                generationRow.addView(
                    button,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        dp(36)
                    ).apply {
                        marginEnd = dp(7)
                    }
                )
            }
        }

        fun refreshCategoryButtons() {
            categoryRow.removeAllViews()

            categories.forEach { category ->
                val button = makeFilterButton(
                    category,
                    selectedCategory == category
                )

                button.setOnClickListener {
                    selectedCategory = category
                    selectedGeneration = 0

                    refreshCategoryButtons()
                    refreshGenerationButtons()
                    refreshResults()
                }

                categoryRow.addView(
                    button,
                    LinearLayout.LayoutParams(
                        0,
                        dp(42),
                        1f
                    ).apply {
                        marginEnd = dp(5)
                    }
                )
            }
        }

        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                searchQuery = s?.toString()?.trim().orEmpty()
                refreshResults()
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        refreshCategoryButtons()
        refreshGenerationButtons()
        refreshResults()
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
