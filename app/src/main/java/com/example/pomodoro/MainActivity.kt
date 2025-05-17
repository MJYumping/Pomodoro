package com.example.pomodoro;

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var timerTextView: TextView
    private lateinit var startButton: Button
    private lateinit var pauseButton: Button
    private lateinit var resetButton: Button

    private var countDownTimer: CountDownTimer? = null
    private val workDuration = 25 * 60 * 1000L
    private val breakDuration = 5 * 60 * 1000L

    private var isWorkPhase = true
    private var isTimerRunning = false
    private var isPaused = false
    private var timeLeftInMillis: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        resetButton = findViewById(R.id.resetButton)
        timerTextView = findViewById(R.id.timerTextView)
        startButton = findViewById(R.id.startButton)
        pauseButton = findViewById(R.id.pauseButton)

        // Show popup only once (first launch)
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val hasShownPopup = prefs.getBoolean("hasShownPopup", false)

        if (!hasShownPopup) {
            showInstructionsPopup()
            prefs.edit().putBoolean("hasShownPopup", true).apply()
        }

        startButton.setOnClickListener {
            if (!isTimerRunning) {
                isWorkPhase = true
                timeLeftInMillis = 0L // reset timer
                startPomodoroTimer()
            }
        }

        pauseButton.setOnClickListener {
            if (isTimerRunning && !isPaused) {
                pauseTimer()
                pauseButton.text = "Resume"
            } else if (isPaused) {
                resumeTimer()
                pauseButton.text = "Pause"
            }
        }

        resetButton.setOnClickListener {
            resetTimer()
        }

        val popupButton = findViewById<Button>(R.id.popupButton)
        popupButton.setOnClickListener {
            showInstructionsPopup()
        }
    }

    private fun showInstructionsPopup() {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.popup_dialog, null)
        dialog.setContentView(view)

        val closeButton = view.findViewById<Button>(R.id.closeButton)
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setCancelable(false)
        dialog.show()

        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun startPomodoroTimer() {
        isTimerRunning = true
        isPaused = false

        val baseDuration = if (isWorkPhase) workDuration else breakDuration
        if (timeLeftInMillis == 0L) timeLeftInMillis = baseDuration

        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = (millisUntilFinished / 1000) % 60
                timerTextView.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                isTimerRunning = false
                isPaused = false
                timeLeftInMillis = 0L

                if (isWorkPhase) {
                    isWorkPhase = false
                    timerTextView.postDelayed({
                        startPomodoroTimer()
                    }, 1000)
                } else {
                    timerTextView.text = "00:00"
                }
            }
        }.start()
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        isPaused = true
    }

    private fun resumeTimer() {
        startPomodoroTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        isPaused = false
        timeLeftInMillis = 0L
        isWorkPhase = true

        timerTextView.text = String.format("%02d:%02d", workDuration / 60000, (workDuration / 1000) % 60)
        pauseButton.text = "Pause"
    }
}