package com.zakss.jkt48universe

import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.VideoView

class MainActivity : Activity() {

    private val pink = Color.rgb(232, 70, 145)
    private val palePink = Color.rgb(255, 242, 248)
    private val dark = Color.rgb(55, 35, 48)
    private val muted = Color.rgb(125, 105, 117)

    private val handler = Handler(Looper.getMainLooper())
    private var videoView: VideoView? = null
    private var progressBar: ProgressBar? = null

    private val quizQuestions = listOf(
        "Apa kepanjangan JKT48?" to listOf(
            "Jakarta 48", "Japan 48", "Junior K-pop Team"
        ),
        "JKT48 merupakan grup idola yang berasal dari negara mana?" to listOf(
            "Indonesia", "Jepang", "Korea Selatan"
        ),
        "JKT48 dikenal menggunakan konsep yang terinspirasi dari grup mana?" to listOf(
            "AKB48", "BLACKPINK", "TWICE"
        )
    )

    private val quizAnswers = listOf(0, 0, 0)
    private var currentQuestion = 0
    private var score = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showIntro()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun showIntro() {
        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
        }

        val video = VideoView(this)
        videoView = video

        val bar = ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            max = 100
            progress = 0
            progressTintList = android.content.res.ColorStateList.valueOf(pink)
            progressBackgroundTintList =
                android.content.res.ColorStateList.valueOf(Color.LTGRAY)
        }
        progressBar = bar

        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(28), dp(12), dp(28), dp(28))
        }

        val label = TextView(this).apply {
            text = "JKT48 UNIVERSE"
            textSize = 13f
            letterSpacing = 0.16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        bottom.addView(
            label,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(28)
            )
        )
        bottom.addView(
            bar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(5)
            )
        )

        root.addView(
            video,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val bottomParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM
        )
        root.addView(bottom, bottomParams)

        setContentView(root)

        try {
            val videoUri = Uri.parse(
                "android.resource://$packageName/${R.raw.intro}"
            )
            video.setVideoURI(videoUri)

            video.setOnPreparedListener { player ->
                player.isLooping = false
                video.start()
                updateVideoProgress()
            }

            video.setOnCompletionListener {
                handler.removeCallbacksAndMessages(null)
                showHome()
            }

            video.setOnErrorListener { _, _, _ ->
                handler.removeCallbacksAndMessages(null)
                showHome()
                true
            }
        } catch (_: Exception) {
            showHome()
        }
    }

    private fun updateVideoProgress() {
        val video = videoView ?: return
        val bar = progressBar ?: return

        if (video.isPlaying) {
            val duration = video.duration
            if (duration > 0) {
                bar.progress = (video.currentPosition * 100) / duration
            }
        }

        handler.postDelayed({ updateVideoProgress() }, 250)
    }

    private fun showHome() {
        handler.removeCallbacksAndMessages(null)
        videoView = null
        progressBar = null

        val content = pageContainer()

        content.addView(
            heading("JKT48 UNIVERSE", 28f, pink)
        )
        content.addView(
            paragraph("Your little universe of idols and stories.")
        )
        content.addView(
            paragraph(
                "Unofficial fan-made project. Not affiliated with or endorsed by JKT48."
            )
        )

        sectionTitle(content, "EXPLORE")

        val menuItems = listOf(
            "Members" to "Jelajahi profil anggota dan trainee.",
            "Events" to "Lihat contoh informasi event.",
            "Chat" to "Ruang percakapan karakter virtual.",
            "Quiz" to "Uji pengetahuan JKT48 kamu.",
            "Gallery" to "Galeri foto dan kenangan.",
            "News" to "Berita dan informasi terbaru."
        )

        menuItems.forEach { (name, description) ->
            addMenuCard(content, name, description) {
                when (name) {
                    "Members" -> showMembers()
                    "Events" -> showEvents()
                    "Chat" -> showChat()
                    "Quiz" -> startQuiz()
                    "Gallery" -> showGallery()
                    "News" -> showNews()
                }
            }
        }

        addBottomNavigation(content)
        setPage(content)
    }

    private fun showMembers() {
        val content = pageContainer()
        addBackButton(content)
        content.addView(heading("Members", 27f, pink))
        content.addView(
            paragraph("Profil contoh. Data anggota resmi akan ditambahkan setelah tahap database.")
        )

        addInfoCard(
            content,
            "Member 01",
            "Profil contoh anggota. Informasi ini bukan data anggota sungguhan."
        )
        addInfoCard(
            content,
            "Member 02",
            "Profil contoh anggota. Foto dan biodata resmi perlu sumber yang diizinkan."
        )
        addInfoCard(
            content,
            "Trainee",
            "Daftar trainee akan diperbarui setelah sumber data disiapkan."
        )

        addBottomNavigation(content)
        setPage(content)
    }

    private fun showEvents() {
        val content = pageContainer()
        addBackButton(content)
        content.addView(heading("Events", 27f, pink))
        content.addView(
            paragraph("Informasi contoh untuk tampilan awal aplikasi.")
        )
        addInfoCard(
            content,
            "Event information",
            "Jadwal pertunjukan belum terhubung ke sumber resmi."
        )
        addInfoCard(
            content,
            "Schedule updates",
            "Fitur pembaruan jadwal otomatis akan dibuat pada tahap database."
        )
        addBottomNavigation(content)
        setPage(content)
    }

    private fun showChat() {
        val content = pageContainer()
        addBackButton(content)
        content.addView(heading("Chat", 27f, pink))
        content.addView(
            paragraph(
                "Ruang karakter virtual. Fitur percakapan AI sungguhan belum aktif."
            )
        )
        addInfoCard(
            content,
            "Virtual character",
            "Karakter di sini merupakan simulasi digital, bukan anggota JKT48 yang sedang membalas pesan."
        )
        addBottomNavigation(content)
        setPage(content)
    }

    private fun startQuiz() {
        currentQuestion = 0
        score = 0
        showQuizQuestion()
    }

    private fun showQuizQuestion() {
        val content = pageContainer()
        addBackButton(content)

        if (currentQuestion >= quizQuestions.size) {
            content.addView(heading("Quiz selesai!", 27f, pink))
            content.addView(
                paragraph("Skor kamu: $score dari ${quizQuestions.size}")
            )
            addActionButton(content, "Ulangi Quiz") {
                startQuiz()
            }
            addBottomNavigation(content)
            setPage(content)
            return
        }

        val question = quizQuestions[currentQuestion]
        content.addView(
            heading("Quiz ${currentQuestion + 1}/${quizQuestions.size}", 24f, pink)
        )
        content.addView(paragraph(question.first))

        question.second.forEachIndexed { index, answer ->
            addActionButton(content, answer) {
                if (index == quizAnswers[currentQuestion]) {
                    score++
                }
                currentQuestion++
                showQuizQuestion()
            }
        }

        addBottomNavigation(content)
        setPage(content)
    }

    private fun showGallery() {
        val content = pageContainer()
        addBackButton(content)
        content.addView(heading("Gallery", 27f, pink))
        content.addView(
            paragraph("Galeri akan diisi dengan aset yang memiliki izin penggunaan.")
        )
        addInfoCard(
            content,
            "Gallery collection",
            "Belum ada foto yang ditambahkan."
        )
        addBottomNavigation(content)
        setPage(content)
    }

    private fun showNews() {
        val content = pageContainer()
        addBackButton(content)
        content.addView(heading("News", 27f, pink))
        content.addView(
            paragraph("Berita contoh untuk versi pertama.")
        )
        addInfoCard(
            content,
            "Welcome to JKT48 Universe",
            "Halaman berita sudah tersedia. Sumber berita resmi akan ditambahkan kemudian."
        )
        addBottomNavigation(content)
        setPage(content)
    }

    private fun pageContainer(): LinearLayout {
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(28), dp(20), dp(28))
            setBackgroundColor(palePink)
        }
        scroll.addView(content)
        scroll.tag = "pageScroll"
        currentRoot = scroll
        return content
    }

    private var currentRoot: View? = null

    private fun setPage(content: LinearLayout) {
        val scroll = currentRoot as? ScrollView ?: return
        scroll.removeAllViews()
        scroll.addView(content)
        setContentView(scroll)
    }

    private fun heading(
        text: String,
        size: Float,
        color: Int
    ): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, dp(8), 0, dp(8))
        }
    }

    private fun paragraph(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(muted)
            setPadding(0, dp(4), 0, dp(12))
        }
    }

    private fun sectionTitle(
        parent: LinearLayout,
        text: String
    ) {
        parent.addView(
            heading(text, 15f, dark).apply {
                setPadding(0, dp(18), 0, dp(8))
            }
        )
    }

    private fun addMenuCard(
        parent: LinearLayout,
        title: String,
        description: String,
        action: () -> Unit
    ) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            setBackgroundColor(Color.WHITE)
        }

        card.addView(heading(title, 18f, dark))
        card.addView(paragraph(description))

        val button = Button(this).apply {
            text = "Open $title"
            setTextColor(Color.WHITE)
            backgroundTintList =
                android.content.res.ColorStateList.valueOf(pink)
            setOnClickListener { action() }
        }
        card.addView(button)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = dp(12)
        parent.addView(card, params)
    }

    private fun addInfoCard(
        parent: LinearLayout,
        title: String,
        description: String
    ) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
            setBackgroundColor(Color.WHITE)
        }
        card.addView(heading(title, 18f, dark))
        card.addView(paragraph(description))

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = dp(12)
        parent.addView(card, params)
    }

    private fun addActionButton(
        parent: LinearLayout,
        title: String,
        action: () -> Unit
    ) {
        val button = Button(this).apply {
            text = title
            setTextColor(Color.WHITE)
            backgroundTintList =
                android.content.res.ColorStateList.valueOf(pink)
            setOnClickListener { action() }
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = dp(8)
        parent.addView(button, params)
    }

    private fun addBackButton(parent: LinearLayout) {
        addActionButton(parent, "← Back to Home") {
            showHome()
        }
    }

    private fun addBottomNavigation(parent: LinearLayout) {
        sectionTitle(parent, "NAVIGATION")

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        listOf("Home", "Members", "Events", "News").forEach { item ->
            val button = Button(this).apply {
                text = item
                textSize = 10f
                setOnClickListener {
                    when (item) {
                        "Home" -> showHome()
                        "Members" -> showMembers()
                        "Events" -> showEvents()
                        "News" -> showNews()
                    }
                }
            }
            nav.addView(
                button,
                LinearLayout.LayoutParams(0, dp(48), 1f)
            )
        }

        parent.addView(nav)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        videoView?.stopPlayback()
        super.onDestroy()
    }
}
