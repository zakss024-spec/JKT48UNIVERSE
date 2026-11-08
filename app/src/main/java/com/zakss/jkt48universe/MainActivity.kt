package com.zakss.jkt48universe

import android.app.Activity
import android.app.AlertDialog
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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@UnstableApi
class MainActivity : Activity() {

    private var player: ExoPlayer? = null
    private lateinit var root: FrameLayout

    private val handler = Handler(Looper.getMainLooper())
    private var progressRunnable: Runnable? = null
    private var introFinished = false

    private val activityScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)

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

    private fun addSectionTitle(
        parent: LinearLayout,
        text: String
    ) {
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

    private fun addInfoCard(
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

    // ==========================================
    // HOME AND NAVIGATION
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

            pageLayout.addView(makeText(page, 25f, dark, true))

            when (page) {
                "Home" -> showHomeContent(pageLayout)
                "Members" -> showMembersDirectory(pageLayout)
                "Events" -> showEvents(pageLayout)
                "Chat" -> showChat(pageLayout)
                "Quiz" -> showQuiz(pageLayout)
                "Gallery" -> showGallery(pageLayout)
                "News" -> showNews(pageLayout)
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

            menuButton.addView(
                makeText(icon, 29f, dark, true).apply {
                    gravity = Gravity.CENTER
                },
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(36)
                )
            )

            menuButton.addView(
                makeText(label, 12f, Color.DKGRAY).apply {
                    gravity = Gravity.CENTER
                    maxLines = 1
                },
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(22)
                )
            )

            val indicator = View(this).apply {
                background = makeBackground(pink, 10)
                visibility = View.INVISIBLE
            }

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
    // HOME CONTENT
    // ==========================================

    private fun showHomeContent(parent: LinearLayout) {
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

        banner.addView(
            makeText(
                "Satu tempat untuk menjelajahi dunia JKT48.",
                14f,
                Color.WHITE
            ).apply {
                setPadding(0, dp(8), 0, 0)
            }
        )

        parent.addView(
            banner,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(18)
            }
        )

        addSectionTitle(parent, "♡ Member Spotlight")
        addInfoCard(
            parent,
            "Member pilihan",
            "Profil member tersedia melalui menu Members.",
            "🌸"
        )

        addSectionTitle(parent, "✦ Jelajahi Menu")

        val quickMenu = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        listOf(
            Triple("♡", "Members", "Members"),
            Triple("★", "Events", "Events"),
            Triple("✦", "Quiz", "Quiz")
        ).forEach { item ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(8), dp(15), dp(8), dp(15))
                background = makeBackground(Color.WHITE, 16)
                isClickable = true
                isFocusable = true

                setOnClickListener {
                    Toast.makeText(
                        this@MainActivity,
                        "Gunakan menu navigasi bawah untuk membuka ${item.third}.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            card.addView(
                makeText(item.first, 25f, pink).apply {
                    gravity = Gravity.CENTER
                }
            )

            card.addView(
                makeText(item.second, 12f, dark, true).apply {
                    gravity = Gravity.CENTER
                }
            )

            quickMenu.addView(
                card,
                LinearLayout.LayoutParams(
                    0,
                    dp(85),
                    1f
                ).apply {
                    marginStart = dp(5)
                    marginEnd = dp(5)
                }
            )
        }

        parent.addView(quickMenu)

        addSectionTitle(parent, "★ Events")
        addInfoCard(
            parent,
            "Event JKT48",
            "Lihat halaman Events untuk daftar jadwal dan kegiatan.",
            "🎤"
        )

        addSectionTitle(parent, "♫ Update Terbaru")
        addInfoCard(
            parent,
            "Universe Update",
            "Lihat halaman News untuk ringkasan informasi.",
            "📰"
        )
    }

    // ==========================================
    // MEMBERS DIRECTORY - SUPABASE
    // ==========================================

    private fun showMembersDirectory(parent: LinearLayout) {

        var selectedCategory = "Aktif"
        var selectedGeneration = 0
        var searchQuery = ""

        var loadedMembers = emptyList<MemberProfile>()
        var isLoading = true
        var loadError: String? = null

        val categories = listOf("Aktif", "Trainee", "Alumni")

        parent.addView(
            makeText(
                "Jelajahi profil member JKT48.",
                14f,
                Color.DKGRAY
            ).apply {
                setPadding(0, dp(10), 0, dp(16))
            }
        )

        val searchBox = EditText(this).apply {
            hint = "Cari nama member..."
            textSize = 14f
            setSingleLine(true)
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

        fun showMemberDetail(member: MemberProfile) {
            val detail = buildString {
                append("Nama: ${member.name}\n")

                member.nickname?.let {
                    append("Panggilan: $it\n")
                }

                member.generation?.let {
                    append("Generasi: $it\n")
                }

                append(
                    "Status: ${
                        when (member.status) {
                            "active" -> "Aktif"
                            "trainee" -> "Trainee"
                            else -> "Alumni"
                        }
                    }\n"
                )

                member.team?.let {
                    append("Tim: $it\n")
                }

                member.birthDate?.let {
                    append("Tanggal lahir: $it\n")
                }

                member.height?.let {
                    append("Tinggi: $it cm\n")
                }

                member.bloodType?.let {
                    append("Golongan darah: $it\n")
                }

                member.graduationDate?.let {
                    append("Tanggal kelulusan: $it\n")
                }

                member.bio?.let {
                    append("\n$it")
                }
            }

            AlertDialog.Builder(this)
                .setTitle(member.name)
                .setMessage(detail)
                .setPositiveButton("Tutup", null)
                .show()
        }

        lateinit var loadMembers: () -> Unit

        fun refreshResults() {
            resultArea.removeAllViews()

            val statusFilter = when (selectedCategory) {
                "Aktif" -> "active"
                "Trainee" -> "trainee"
                else -> "alumni"
            }

            val generationText =
                if (selectedGeneration == 0) {
                    "Semua generasi"
                } else {
                    "Generasi $selectedGeneration"
                }

            resultArea.addView(
                makeText(
                    "$selectedCategory • $generationText",
                    15f,
                    dark,
                    true
                )
            )

            if (isLoading) {
                resultArea.addView(
                    makeText(
                        "Mengambil data dari Supabase...",
                        13f,
                        Color.DKGRAY
                    ).apply {
                        setPadding(0, dp(8), 0, dp(10))
                    }
                )

                resultArea.addView(
                    ProgressBar(this).apply {
                        isIndeterminate = true
                    }
                )
                return
            }

            if (loadError != null) {
                val errorCard = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(18), dp(18), dp(18), dp(18))
                    background = makeBackground(Color.WHITE, 18)
                }

                errorCard.addView(
                    makeText(
                        "Gagal mengambil data member",
                        16f,
                        dark,
                        true
                    )
                )

                errorCard.addView(
                    makeText(
                        loadError ?: "Periksa koneksi internet.",
                        13f,
                        Color.DKGRAY
                    ).apply {
                        setPadding(0, dp(8), 0, dp(12))
                    }
                )

                val retryButton = TextView(this).apply {
                    text = "Coba lagi"
                    gravity = Gravity.CENTER
                    setTextColor(Color.WHITE)
                    typeface = Typeface.DEFAULT_BOLD
                    setPadding(dp(14), dp(12), dp(14), dp(12))
                    background = makeBackground(pink, 12)

                    setOnClickListener {
                        loadMembers()
                    }
                }

                errorCard.addView(retryButton)
                resultArea.addView(errorCard)
                return
            }

            val filteredMembers = loadedMembers.filter { member ->

                val matchesStatus =
                    member.status == statusFilter

                val matchesGeneration =
                    selectedGeneration == 0 ||
                        member.generation == selectedGeneration

                val matchesSearch =
                    searchQuery.isBlank() ||
                        member.name.contains(
                            searchQuery,
                            ignoreCase = true
                        ) ||
                        (member.nickname?.contains(
                            searchQuery,
                            ignoreCase = true
                        ) == true)

                matchesStatus &&
                    matchesGeneration &&
                    matchesSearch
            }

            resultArea.addView(
                makeText(
                    "${filteredMembers.size} member ditemukan",
                    13f,
                    pink,
                    true
                ).apply {
                    setPadding(0, dp(8), 0, dp(12))
                }
            )

            if (filteredMembers.isEmpty()) {
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
                        "Member tidak ditemukan",
                        16f,
                        dark,
                        true
                    ).apply {
                        gravity = Gravity.CENTER
                        setPadding(0, dp(10), 0, dp(5))
                    }
                )

                emptyCard.addView(
                    makeText(
                        "Coba ubah kategori, generasi, atau kata pencarian.",
                        13f,
                        Color.DKGRAY
                    ).apply {
                        gravity = Gravity.CENTER
                    }
                )

                resultArea.addView(emptyCard)
                return
            }

            filteredMembers.forEach { member ->

                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(dp(15), dp(15), dp(15), dp(15))
                    background = makeBackground(Color.WHITE, 17)
                    isClickable = true
                    isFocusable = true

                    setOnClickListener {
                        showMemberDetail(member)
                    }
                }

                val initials = member.name
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .joinToString("") {
                        it.first().uppercase()
                    }

                val avatar = TextView(this).apply {
                    text = initials
                    textSize = 17f
                    typeface = Typeface.DEFAULT_BOLD
                    gravity = Gravity.CENTER
                    setTextColor(pink)
                    background = makeBackground(palePink, 50)
                }

                card.addView(
                    avatar,
                    LinearLayout.LayoutParams(
                        dp(52),
                        dp(52)
                    ).apply {
                        marginEnd = dp(13)
                    }
                )

                val information = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                }

                information.addView(
                    makeText(member.name, 15f, dark, true)
                )

                information.addView(
                    makeText(
                        buildString {
                            append(member.nickname ?: "Member")

                            member.generation?.let {
                                append(" • Gen $it")
                            }

                            member.team?.let {
                                append(" • Team $it")
                            }
                        },
                        12f,
                        Color.DKGRAY
                    ).apply {
                        setPadding(0, dp(4), 0, dp(5))
                    }
                )

                val statusLabel = when (member.status) {
                    "active" -> "AKTIF"
                    "trainee" -> "TRAINEE"
                    else -> "ALUMNI"
                }

                information.addView(
                    makeText(statusLabel, 10f, pink, true)
                )

                card.addView(
                    information,
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                )

                card.addView(
                    makeText("›", 26f, pink, true)
                )

                resultArea.addView(
                    card,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = dp(10)
                    }
                )
            }

            animateIn(resultArea)
        }

        fun refreshGenerationButtons() {
            generationRow.removeAllViews()

            val generations = listOf(0) + (1..14).toList()

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

        loadMembers = {
            isLoading = true
            loadError = null
            refreshResults()

            activityScope.launch {
                try {
                    loadedMembers =
                        MemberRepository.getAllMembers()

                    loadError = null

                } catch (error: Exception) {
                    loadError =
                        error.message ?: "Koneksi Supabase gagal."
                }

                isLoading = false
                refreshResults()
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
        loadMembers()
    }

    // ==========================================
    // EVENTS
    // ==========================================

    private fun showEvents(parent: LinearLayout) {
        parent.addView(
            makeText(
                "Temukan jadwal dan kegiatan JKT48.",
                14f,
                Color.DKGRAY
            ).apply {
                setPadding(0, dp(10), 0, dp(15))
            }
        )

        addSectionTitle(parent, "★ Jadwal & Events")

        val events = listOf(
            Triple("07 NOV 2026", "Kenangan Festival 2026", "Event JKT48"),
            Triple("08 NOV 2026", "OFC Event Sudden Versus #4", "Team Dream • 14.15 WIB"),
            Triple("08 NOV 2026", "1st Generation Special Stage", "Homecoming • 16.00 WIB"),
            Triple("11 NOV 2026", "Pertaruhan Cinta", "Theater • 19.00 WIB"),
            Triple("13 NOV 2026", "Pajama Drive", "Trainee • 19.00 WIB"),
            Triple("21 NOV 2026", "ZAP FEST 2026", "Event"),
            Triple("28 NOV 2026", "Healix The Fest 2026", "Bali")
        )

        events.forEach { event ->
            addInfoCard(
                parent,
                event.second,
                "${event.first} • ${event.third}",
                "🎤"
            )
        }

        addInfoCard(
            parent,
            "Catatan jadwal",
            "Jadwal dapat berubah. Periksa situs resmi JKT48 sebelum menghadiri acara.",
            "ℹ"
        )
    }

    // ==========================================
    // CHAT - LOCAL DEMO
    // ==========================================

    private fun showChat(parent: LinearLayout) {
        parent.addView(
            makeText(
                "Ruang komunitas JKT48 Universe.",
                14f,
                Color.DKGRAY
            ).apply {
                setPadding(0, dp(10), 0, dp(15))
            }
        )

        addInfoCard(
            parent,
            "Community Lounge",
            "Chat masih berupa demo lokal. Pesan belum dikirim ke pengguna lain.",
            "♡"
        )

        val chatArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = makeBackground(Color.WHITE, 18)
        }

        val messages = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        messages.addView(
            makeText("Universe Bot", 13f, pink, true)
        )

        messages.addView(
            makeText(
                "Halo! Selamat datang di JKT48 Universe Chat.",
                14f,
                dark
            ).apply {
                setPadding(0, dp(5), 0, dp(12))
            }
        )

        chatArea.addView(messages)

        val input = EditText(this).apply {
            hint = "Tulis pesan..."
            textSize = 14f
            setSingleLine(true)
            setTextColor(dark)
            setHintTextColor(Color.GRAY)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = makeBackground(light, 12)
        }

        chatArea.addView(
            input,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(48)
            ).apply {
                topMargin = dp(10)
            }
        )

        val send = TextView(this).apply {
            text = "Kirim pesan"
            textSize = 14f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            setPadding(dp(15), dp(12), dp(15), dp(12))
            background = makeBackground(pink, 12)

            setOnClickListener {
                if (input.text.toString().trim().isEmpty()) {
                    Toast.makeText(
                        this@MainActivity,
                        "Tulis pesan terlebih dahulu.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Chat online tersedia setelah integrasi database.",
                        Toast.LENGTH_LONG
                    ).show()
                    input.text.clear()
                }
            }
        }

        chatArea.addView(
            send,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(46)
            ).apply {
                topMargin = dp(10)
            }
        )

        parent.addView(
            chatArea,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    // ==========================================
    // QUIZ
    // ==========================================

    private fun showQuiz(parent: LinearLayout) {
        parent.addView(
            makeText(
                "Uji pengetahuanmu tentang JKT48!",
                14f,
                Color.DKGRAY
            ).apply {
                setPadding(0, dp(10), 0, dp(15))
            }
        )

        addSectionTitle(parent, "✦ JKT48 Quiz")

        val questions = listOf(
            Triple(
                "JKT48 merupakan sister group dari grup apa?",
                listOf("AKB48", "Nogizaka46", "SKE48"),
                0
            ),
            Triple(
                "JKT48 berasal dari negara mana?",
                listOf("Jepang", "Indonesia", "Thailand"),
                1
            ),
            Triple(
                "Di kota mana JKT48 berbasis?",
                listOf("Jakarta", "Bandung", "Surabaya"),
                0
            ),
            Triple(
                "JKT48 termasuk keluarga grup apa?",
                listOf("46 Group", "48 Group", "Hello! Project"),
                1
            ),
            Triple(
                "Apa nama tempat pertunjukan reguler JKT48?",
                listOf("Theater", "Stadion", "Museum"),
                0
            )
        )

        val quizArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = makeBackground(Color.WHITE, 18)
        }

        val questionNumber = makeText("", 13f, pink, true)
        val questionText = makeText("", 18f, dark, true)
        val answerArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        val feedback = makeText("", 14f, dark, true)
        val scoreText = makeText("", 14f, pink, true)

        quizArea.addView(questionNumber)

        quizArea.addView(
            questionText.apply {
                setPadding(0, dp(10), 0, dp(15))
            }
        )

        quizArea.addView(answerArea)

        quizArea.addView(
            feedback.apply {
                setPadding(0, dp(12), 0, dp(5))
            }
        )

        quizArea.addView(scoreText)
        parent.addView(quizArea)

        var currentQuestion = 0
        var score = 0
        var answered = false
        var nextRunnable: Runnable? = null

        fun showQuestion() {
            nextRunnable?.let { handler.removeCallbacks(it) }
            nextRunnable = null

            answerArea.removeAllViews()
            feedback.text = ""
            answered = false

            if (currentQuestion >= questions.size) {
                questionNumber.text = "QUIZ SELESAI"
                questionText.text =
                    "Skor kamu: $score dari ${questions.size}"
                scoreText.text = "Terima kasih sudah bermain 🌸"

                val retry = TextView(this).apply {
                    text = "Ulangi Quiz"
                    gravity = Gravity.CENTER
                    textSize = 14f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(Color.WHITE)
                    setPadding(dp(15), dp(13), dp(15), dp(13))
                    background = makeBackground(pink, 12)

                    setOnClickListener {
                        currentQuestion = 0
                        score = 0
                        showQuestion()
                    }
                }

                answerArea.addView(retry)
                return
            }

            val question = questions[currentQuestion]

            questionNumber.text =
                "PERTANYAAN ${currentQuestion + 1} / ${questions.size}"

            questionText.text = question.first
            scoreText.text = "Skor: $score"

            question.second.forEachIndexed { index, answer ->
                val button = TextView(this).apply {
                    text = answer
                    textSize = 14f
                    gravity = Gravity.CENTER_VERTICAL
                    setTextColor(dark)
                    setPadding(dp(14), dp(13), dp(14), dp(13))
                    background = makeBackground(light, 12)
                    isClickable = true
                    isFocusable = true

                    setOnClickListener {
                        if (answered) return@setOnClickListener
                        answered = true

                        if (index == question.third) {
                            score++
                            feedback.text = "Benar! 🎉"
                            feedback.setTextColor(
                                Color.rgb(35, 145, 85)
                            )
                        } else {
                            feedback.text =
                                "Belum tepat. Jawaban: ${question.second[question.third]}"
                            feedback.setTextColor(
                                Color.rgb(190, 65, 75)
                            )
                        }

                        scoreText.text = "Skor: $score"

                        nextRunnable = Runnable {
                            currentQuestion++
                            showQuestion()
                        }

                        handler.postDelayed(nextRunnable!!, 1100)
                    }
                }

                answerArea.addView(
                    button,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(46)
                    ).apply {
                        bottomMargin = dp(8)
                    }
                )
            }
        }

        showQuestion()
    }

    // ==========================================
    // GALLERY
    // ==========================================

    private fun showGallery(parent: LinearLayout) {
        parent.addView(
            makeText(
                "Koleksi album dan momen JKT48.",
                14f,
                Color.DKGRAY
            ).apply {
                setPadding(0, dp(10), 0, dp(15))
            }
        )

        addSectionTitle(parent, "▣ Album Gallery")

        addInfoCard(
            parent,
            "Member Moments",
            "Album khusus momen para member JKT48.",
            "📸"
        )

        addInfoCard(
            parent,
            "Theater Memories",
            "Kumpulan kenangan pertunjukan theater.",
            "🎭"
        )

        addInfoCard(
            parent,
            "Event Collection",
            "Album dokumentasi event dan kegiatan JKT48.",
            "🎤"
        )

        addInfoCard(
            parent,
            "Galeri sedang disiapkan",
            "Foto resmi akan ditambahkan setelah sumber dan izin penggunaannya diperiksa.",
            "🌸"
        )
    }

    // ==========================================
    // NEWS
    // ==========================================

    private fun showNews(parent: LinearLayout) {
        parent.addView(
            makeText(
                "Informasi dan kabar JKT48.",
                14f,
                Color.DKGRAY
            ).apply {
                setPadding(0, dp(10), 0, dp(15))
            }
        )

        addSectionTitle(parent, "♫ Universe News")

        val news = listOf(
            Triple(
                "04 NOV 2026",
                "Theater Special Charity Show",
                "Informasi pertunjukan spesial JKT48."
            ),
            Triple(
                "29 OCT 2026",
                "1st Generation Special Stage",
                "Homecoming: Where It All Began."
            ),
            Triple(
                "28 OCT 2026",
                "Pengumuman Video Call",
                "Informasi penggantian member Helisma Putri."
            ),
            Triple(
                "27 OCT 2026",
                "OFC Event Sudden Versus #4",
                "Informasi event Mancing Ikuzo!."
            ),
            Triple(
                "26 OCT 2026",
                "2-Shot Mini Tour",
                "Penjualan tiket Ayo Senyumlah."
            )
        )

        news.forEach { item ->
            addInfoCard(
                parent,
                item.second,
                "${item.first}\n${item.third}",
                "📰"
            )
        }

        addInfoCard(
            parent,
            "Sumber berita",
            "Periksa situs resmi JKT48 untuk pengumuman dan informasi terbaru.",
            "🌐"
        )
    }

    // ==========================================
    // ACTIVITY LIFECYCLE
    // ==========================================

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onDestroy() {
        activityScope.cancel()
        releasePlayer()
        super.onDestroy()
    }
}

