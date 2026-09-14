package com.example.emailaliassandbox

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var addressView: TextView
    private lateinit var expiryView: TextView
    private lateinit var inboxView: TextView
    private lateinit var statusView: TextView
    private lateinit var copyButton: Button
    private var timer: CountDownTimer? = null
    private var currentAlias: String? = null
    private val cardColor = Color.rgb(247, 247, 250)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildUi())
        generateAlias()
    }

    private fun buildUi(): ScrollView {
        val scroll = ScrollView(this).apply { isFillViewport = true }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(28))
        }

        root.addView(label("EMAIL ALIAS SANDBOX"), lp())
        root.addView(title("Your test inbox,\nwithout the real inbox"), lp(0, 6))
        root.addView(body("Generate a local test address, copy it, and simulate a verification email. Nothing is sent to a real mail provider."), lp(0, 18))

        val addressCard = card()
        addressCard.addView(label("CURRENT TEST ADDRESS"), lp())
        addressView = TextView(this).apply {
            textSize = 19f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.rgb(35, 35, 45))
            setPadding(0, dp(8), 0, dp(14))
            gravity = Gravity.CENTER
        }
        addressCard.addView(addressView, lp())
        copyButton = Button(this).apply {
            text = "Copy address"
            isAllCaps = false
            setOnClickListener { copyAlias() }
        }
        addressCard.addView(copyButton, lp())
        root.addView(addressCard, lp(0, 4))

        val generate = Button(this).apply {
            text = "Generate new address"
            isAllCaps = false
            setOnClickListener { generateAlias() }
        }
        root.addView(generate, lp(0, 10))

        val expiryCard = card()
        expiryCard.addView(label("SESSION STATUS"), lp())
        expiryView = TextView(this).apply {
            textSize = 22f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.rgb(35, 35, 45))
            setPadding(0, dp(6), 0, dp(2))
        }
        expiryCard.addView(expiryView, lp())
        statusView = body("A new test session lasts 15 minutes.")
        expiryCard.addView(statusView, lp(0, 4))
        root.addView(expiryCard, lp(0, 16))

        root.addView(label("MOCK INBOX"), lp(0, 24))
        inboxView = body("No messages yet.\n\nTap the button below to simulate a verification email." ).apply {
            setPadding(dp(14), dp(14), dp(14), dp(14))
            setBackgroundColor(cardColor)
        }
        root.addView(inboxView, lp())

        root.addView(Button(this).apply {
            text = "Add sample verification email"
            isAllCaps = false
            setOnClickListener { addSampleMessage() }
        }, lp(0, 10))

        root.addView(body("Privacy note: this demo uses example.test and stores everything only in the app UI."), lp(0, 20))
        scroll.addView(root)
        return scroll
    }

    private fun generateAlias() {
        timer?.cancel()
        val token = buildString {
            repeat(10) { append("abcdefghijklmnopqrstuvwxyz0123456789"[Random.nextInt(36)]) }
        }
        currentAlias = "test-$token@example.test"
        addressView.text = currentAlias
        copyButton.isEnabled = true
        expiryView.text = "15:00 remaining"
        statusView.text = "Active • local test session"
        inboxView.text = "No messages yet.\n\nTap the button below to simulate a verification email."
        timer = object : CountDownTimer(15 * 60 * 1000L, 1000L) {
            override fun onTick(ms: Long) {
                expiryView.text = "${formatMs(ms)} remaining"
            }
            override fun onFinish() {
                expiryView.text = "Expired"
                statusView.text = "Expired • generate a new address to continue"
                addressView.text = "Session expired"
                copyButton.isEnabled = false
                currentAlias = null
            }
        }.start()
        Toast.makeText(this, "New test address ready", Toast.LENGTH_SHORT).show()
    }

    private fun copyAlias() {
        val alias = currentAlias ?: return
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("test email", alias))
        Toast.makeText(this, "Address copied", Toast.LENGTH_SHORT).show()
    }

    private fun addSampleMessage() {
        if (currentAlias == null) {
            Toast.makeText(this, "Generate a new address first", Toast.LENGTH_SHORT).show()
            return
        }
        inboxView.text = "Example App\n\nYour verification code\n\n483 271\n\nSimulated message • stored only in this demo"
        Toast.makeText(this, "Sample email added", Toast.LENGTH_SHORT).show()
    }

    private fun label(text: String) = TextView(this).apply {
        this.text = text
        textSize = 12f
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(Color.rgb(95, 95, 110))
    }

    private fun title(text: String) = TextView(this).apply {
        this.text = text
        textSize = 30f
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(Color.rgb(25, 25, 32))
    }

    private fun body(text: String) = TextView(this).apply {
        this.text = text
        textSize = 15f
        setTextColor(Color.rgb(80, 80, 92))
        setLineSpacing(0f, 1.15f)
    }

    private fun card() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(16), dp(16), dp(16))
        setBackgroundColor(cardColor)
    }

    private fun lp(top: Int = 0, extraTop: Int = 0): LinearLayout.LayoutParams =
        LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(top + extraTop)
        }

    private fun formatMs(ms: Long): String {
        val totalSeconds = ms / 1000
        return String.format(Locale.US, "%02d:%02d", totalSeconds / 60, totalSeconds % 60)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
