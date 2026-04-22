package com.boostx

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var tvRamUsed: TextView
    private lateinit var tvRamFree: TextView
    private lateinit var progressRam: ProgressBar
    private lateinit var btnBoost: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvAnimSpeed: TextView
    private lateinit var switchAnimator: Switch

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        startRamMonitor()
        setupBoostButton()
        setupAnimatorSwitch()
    }

    private fun initViews() {
        tvRamUsed = findViewById(R.id.tvRamUsed)
        tvRamFree = findViewById(R.id.tvRamFree)
        progressRam = findViewById(R.id.progressRam)
        btnBoost = findViewById(R.id.btnBoost)
        tvStatus = findViewById(R.id.tvStatus)
        tvAnimSpeed = findViewById(R.id.tvAnimSpeed)
        switchAnimator = findViewById(R.id.switchAnimator)
    }

    private val ramRunnable = object : Runnable {
        override fun run() {
            updateRamInfo()
            handler.postDelayed(this, 2000)
        }
    }

    private fun startRamMonitor() = handler.post(ramRunnable)

    private fun updateRamInfo() {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        val totalMb = mi.totalMem / 1048576
        val availMb = mi.availMem / 1048576
        val usedMb = totalMb - availMb
        val pct = ((usedMb.toFloat() / totalMb) * 100).toInt()
        tvRamUsed.text = "Đã dùng: ${usedMb} MB"
        tvRamFree.text = "Còn trống: ${availMb} MB / ${totalMb} MB"
        progressRam.progress = pct
    }

    private fun setupBoostButton() {
        btnBoost.setOnClickListener {
            tvStatus.text = "⚡ Đang tối ưu..."
            btnBoost.isEnabled = false
            Runtime.getRuntime().gc()
            System.gc()
            clearCacheDir(cacheDir)
            handler.postDelayed({
                updateRamInfo()
                tvStatus.text = "✅ Tối ưu hoàn tất!"
                btnBoost.isEnabled = true
            }, 1500)
        }
    }

    private fun clearCacheDir(dir: File) {
        dir.listFiles()?.forEach {
            if (it.isDirectory) clearCacheDir(it) else it.delete()
        }
    }

    private fun setupAnimatorSwitch() {
        val scale = android.provider.Settings.Global.getFloat(
            contentResolver,
            android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f)
        tvAnimSpeed.text = "Animation hiện tại: ${scale}x"
        switchAnimator.isChecked = scale <= 0.5f

        switchAnimator.setOnCheckedChangeListener { _, isChecked ->
            val newScale = if (isChecked) 0.5f else 1.0f
            try {
                android.provider.Settings.Global.putFloat(contentResolver,
                    android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, newScale)
                android.provider.Settings.Global.putFloat(contentResolver,
                    android.provider.Settings.Global.WINDOW_ANIMATION_SCALE, newScale)
                android.provider.Settings.Global.putFloat(contentResolver,
                    android.provider.Settings.Global.TRANSITION_ANIMATION_SCALE, newScale)
                tvAnimSpeed.text = "Animation: ${newScale}x"
                Toast.makeText(this,
                    if (isChecked) "🚀 Animation nhanh hơn!" else "↩️ Đã khôi phục",
                    Toast.LENGTH_SHORT).show()
            } catch (e: SecurityException) {
                Toast.makeText(this,
                    "Cần cấp quyền 'Thay đổi cài đặt hệ thống'",
                    Toast.LENGTH_LONG).show()
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data
