package com.zakss.jkt48universe

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import io.github.jan.supabase.auth.providers.Email
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AuthActivity : Activity() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val pink = Color.rgb(238, 91, 158)
    private val dark = Color.rgb(35, 25, 42)
    private val light = Color.rgb(255, 244, 250)

    private lateinit var usernameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmInput: EditText
    private lateinit var message: TextView
    private lateinit var submitButton: TextView
    private lateinit var modeButton: TextView

    private var isRegisterMode = false
    private var busy = false

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = dark
        window.navigationBarColor = dark

        // Periksa sesi yang tersimpan.
        scope.launch {
            try {
                val session =
                    SupabaseClient.client.auth.currentSessionOrNull()

                if (session != null) {
                    openMain()
                } else {
                    showAuthScreen()
                }
            } catch (_: Exception) {
                showAuthScreen()
            }
        }
    }

    private fun background(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
    }

    private fun text(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }
    }

    private fun input(hintText: String, password: Boolean = false): EditText {
        return EditText(this).apply {
            hint = hintText
            textSize = 15f
            setTextColor(dark)
            setHintTextColor(Color.GRAY)
            setPadding(dp(15), dp(12), dp(15), dp(12))
            background = background(Color.WHITE, 14)
            maxLines = 1

            if (password) {
                inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                inputType =
                    android.text.InputType.TYPE_CLASS_TEXT
            }
        }
    }

    private fun showAuthScreen() {
        val scroll = ScrollView(this).apply {
            setBackgroundColor(light)
            isFillViewport = true
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(25), dp(35), dp(25), dp(35))
        }

        scroll.addView(container)
        setContentView(scroll)

        container.addView(
            text("JKT48 Universe 🌸", 28f, pink, true).apply {
                gravity = Gravity.CENTER
            }
        )

        container.addView(
            text("Your world of idols ✨", 14f, dark).apply {
                gravity = Gravity.CENTER
                setPadding(0, dp(7), 0, dp(28))
            }
        )

        container.addView(
            text("Selamat datang!", 23f, dark, true)
        )

        container.addView(
            text(
                "Masuk atau buat akun untuk melanjutkan.",
                14f,
                Color.DKGRAY
            ).apply {
                setPadding(0, dp(7), 0, dp(22))
            }
        )

        usernameInput = input("Username")
        emailInput = input("Alamat email")
        passwordInput = input("Password", true)
        confirmInput = input("Ulangi password", true)

        fun addField(field: EditText, bottom: Int = 12) {
            container.addView(
                field,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(54)
                ).apply {
                    bottomMargin = dp(bottom)
                }
            )
        }

        addField(usernameInput)
        addField(emailInput)
        addField(passwordInput)
        addField(confirmInput)

        message = text("", 13f, Color.rgb(180, 50, 70))
        container.addView(
            message,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }
        )

        submitButton = text(
            "LOGIN",
            15f,
            Color.WHITE,
            true
        ).apply {
            gravity = Gravity.CENTER
            background = background(pink, 15)
            isClickable = true
            setPadding(0, dp(15), 0, dp(15))
            setOnClickListener {
                if (isRegisterMode) {
                    register()
                } else {
                    login()
                }
            }
        }

        container.addView(
            submitButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(52)
            )
        )

        modeButton = text(
            "Belum punya akun? Daftar",
            14f,
            pink,
            true
        ).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(22), 0, dp(12))
            setOnClickListener {
                isRegisterMode = !isRegisterMode
                updateMode()
            }
        }

        container.addView(modeButton)

        updateMode()
    }

    private fun updateMode() {
        usernameInput.visibility =
            if (isRegisterMode) android.view.View.VISIBLE
            else android.view.View.GONE

        confirmInput.visibility =
            if (isRegisterMode) android.view.View.VISIBLE
            else android.view.View.GONE

        submitButton.text =
            if (isRegisterMode) "BUAT AKUN"
            else "LOGIN"

        modeButton.text =
            if (isRegisterMode) {
                "Sudah punya akun? Login"
            } else {
                "Belum punya akun? Daftar"
            }

        message.text = ""
    }

    private fun setBusy(value: Boolean) {
        busy = value
        submitButton.isEnabled = !value
        submitButton.alpha = if (value) 0.6f else 1f

        submitButton.text = when {
            value -> "MEMPROSES..."
            isRegisterMode -> "BUAT AKUN"
            else -> "LOGIN"
        }
    }

    private fun login() {
        if (busy) return

        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()

        if (email.isBlank() || password.isBlank()) {
            message.text = "Email dan password harus diisi."
            return
        }

        setBusy(true)

        scope.launch {
            try {
                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }

                openMain()

            } catch (error: Exception) {
                message.text =
                    "Login gagal: ${error.message ?: "Periksa email dan password."}"
                setBusy(false)
            }
        }
    }

    private fun register() {
        if (busy) return

        val username = usernameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()
        val confirm = confirmInput.text.toString()

        if (username.length < 3) {
            message.text = "Username minimal 3 karakter."
            return
        }

        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS
                .matcher(email).matches()) {
            message.text = "Masukkan alamat email yang valid."
            return
        }

        if (password.length < 8) {
            message.text = "Password minimal 8 karakter."
            return
        }

        if (password != confirm) {
            message.text = "Konfirmasi password tidak cocok."
            return
        }

        setBusy(true)

        scope.launch {
            try {
                SupabaseClient.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                    data = kotlinx.serialization.json.buildJsonObject {
                        put("username", username)
                    }
                }

                message.setTextColor(Color.rgb(35, 145, 85))
                message.text =
                    "Pendaftaran berhasil! Periksa email untuk verifikasi, lalu login."

                isRegisterMode = false
                updateMode()
                setBusy(false)

            } catch (error: Exception) {
                message.text =
                    "Pendaftaran gagal: ${error.message ?: "Coba lagi."}"
                setBusy(false)
            }
        }
    }

    private fun openMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
