package com.example.emailaliassandbox

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
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
    private var timer: CountDownTimer? = null
    private var currentAlias: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildUi())
        generateAlias()
    }

    private fun buildUi(): ScrollView {
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(24))
        }

        root.addView(TextView(this).apply {
            text = "Email Alias Sandbox"
            textSize = 28f
            setTypeface(typeface, Typeface.BOLD)
        }, lp())

        root.addView(TextView(this).apply {
            text = "Local email-testing playground. Addresses use the reserved example.test domain and do not create real mailboxes."
            textSize = 15f
            setPadding(0, dp(8), 0, dp(20))
        }, lp())

        root.addView(TextView(this).apply {
            text = "TEST ADDRESS"
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
        }, lp())

        addressView = TextView(this).apply {
            textSize = 20f
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setBackgroundColor(0xFFEFEAF7.toInt())
            gravity = Gravity.CENTER
        }
        root.addView(addressView, lp())

        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val generate = Button(this).apply { text = "Generate"; setOnClickListener { generateAlias() } }
        val copy = Button(this).apply { text = "Copy"; setOnClickListener { copyAlias() } }
        actions.addView(generate, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        actions.addView(copy, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        root.addView(actions, lp())

        root.addView(TextView(this).apply {
            text = "Expires in"
            textSize = 12f
            setPadding(0, dp(18), 0, dp(4))
        }, lp())
        expiryView = TextView(this).apply { textSize = 18f; setTypeface(typeface, Typeface.BOLD) }
        root.addView(expiryView, lp())

        root.addView(TextView(this).apply {
            text = "MOCK INBOX"
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, dp(24), 0, dp(8))
        }, lp())

        inboxView = TextView(this).apply {
            textSize = 15f
            text = "No messages yet.\n\nUse this screen to prototype sign-up and verification flows without contacting a real email provider."
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setBackgroundColor(0xFFF7F7F7.toInt())
        }
        root.addView(inboxView, lp())

        root.addView(Button(this).apply {
            text = "Add sample verification email"
            setOnClickListener { addSampleMessage() }
        }, lp())

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
        timer = object : CountDownTimer(15 * 60 * 1000L, 1000L) {
            override fun onTick(ms: Long) { expiryView.text = formatMs(ms) }
            override fun onFinish() {
                expiryView.text = "Expired"
                addressView.text = "(expired)"
            }
        }.start()
        inboxView.text = "No messages yet.\n\nUse this screen to prototype sign-up and verification flows without contacting a real email provider."
    }

    private fun copyAlias() {
        val alias = currentAlias ?: return
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("test email", alias))
        Toast.makeText(this, "Copied test address", Toast.LENGTH_SHORT).show()
    }

    private fun addSampleMessage() {
        inboxView.text = "From: Example App\nSubject: Your verification code\n\nYour test code is 483 271.\n\nThis is simulated content stored only in this app UI."
    }

    private fun formatMs(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun lp() = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
}
