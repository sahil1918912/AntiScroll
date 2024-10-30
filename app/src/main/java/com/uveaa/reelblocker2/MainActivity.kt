package com.uveaa.reelblocker2

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.uveaa.reelblocker2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isBlockingEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val buttonStartBlocking: Button = findViewById(R.id.button_start_blocking)
        val buttonStopBlocking: Button = findViewById(R.id.button_stop_blocking)

        if (!isAccessibilityServiceEnabled(this, ReelBlockerService::class.java)) {
            showPermissionDialog()
        } else {
            showBlockingButtons(buttonStartBlocking, buttonStopBlocking)
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<out AccessibilityService>): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )

        // Return false if no accessibility services are enabled
        if (enabledServices.isNullOrEmpty()) {
            return false
        }

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)

        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            if (componentName.contains(service.name, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Accessibility Permission Required")
            .setMessage("ReelBlocker requires accessibility permission to block Instagram reels. Please enable it in the settings.")
            .setCancelable(false)
            .setPositiveButton("Enable") { _: DialogInterface, _: Int ->
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
                Toast.makeText(this, "Permission is required to block Reels", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showBlockingButtons(buttonStartBlocking: Button, buttonStopBlocking: Button) {
        buttonStartBlocking.setOnClickListener {
            isBlockingEnabled = true
            saveBlockingPreference(true)
            Toast.makeText(this, "Reel and Short blocking started", Toast.LENGTH_SHORT).show()
        }

        buttonStopBlocking.setOnClickListener {
            isBlockingEnabled = false
            saveBlockingPreference(false)
            Toast.makeText(this, "Reel and Short blocking stopped", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBlockingPreference(isEnabled: Boolean) {
        getSharedPreferences("reel_blocker_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("isBlockingEnabled", isEnabled)
            .apply()
    }

    override fun onResume() {
        super.onResume()
        val buttonStartBlocking: Button = findViewById(R.id.button_start_blocking)
        val buttonStopBlocking: Button = findViewById(R.id.button_stop_blocking)

        if (isAccessibilityServiceEnabled(this, ReelBlockerService::class.java)) {
            showBlockingButtons(buttonStartBlocking, buttonStopBlocking)
        }
    }
}