package com.uveaa.reelblocker2

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.SharedPreferences
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import kotlin.collections.isNullOrEmpty
import kotlin.let

class ReelBlockerService : AccessibilityService() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("reel_blocker_prefs", Context.MODE_PRIVATE)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Check if blocking is enabled before proceeding
        if (!isBlockingEnabled()) {
            return // If blocking is disabled, do nothing
        }

        event?.source?.let { node ->
            // Instagram Reels Detection
            if (detectInstagramReel(node)) {
                blockContent() // Block Instagram Reels
            }

            // YouTube Shorts Detection
            if (detectYouTubeShorts(node)) {
                blockContent() // Block YouTube Shorts
            }

            if (detectSnapchatSpotlight(node)) {
                blockContent() // Block SnapChat Spotlights
            }
        }
    }

    private fun isBlockingEnabled(): Boolean {
        // Retrieve the blocking state from SharedPreferences
        return sharedPreferences.getBoolean("isBlockingEnabled", false) // Default to false (disabled)
    }

    // Helper function to detect Instagram Reels
    private fun detectInstagramReel(node: AccessibilityNodeInfo): Boolean {
        val reelsContainer = node.findAccessibilityNodeInfosByViewId("com.instagram.android:id/reel_viewer_root")
        val clipsContainer = node.findAccessibilityNodeInfosByViewId("com.instagram.android:id/clips_video_container")

        return (!reelsContainer.isNullOrEmpty() || !clipsContainer.isNullOrEmpty()) &&
                node.findAccessibilityNodeInfosByViewId("com.instagram.android:id/row_feed_button_like") != null &&
                node.findAccessibilityNodeInfosByViewId("com.instagram.android:id/row_feed_button_comment") != null &&
                node.findAccessibilityNodeInfosByViewId("com.instagram.android:id/row_feed_button_share") != null &&
                node.findAccessibilityNodeInfosByViewId("com.instagram.android:id/row_feed_button_more") != null
    }

    // Helper function to detect YouTube Shorts
    private fun detectYouTubeShorts(node: AccessibilityNodeInfo): Boolean {
        val shortsContainer = node.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/reel_player_page_container")
        return !shortsContainer.isNullOrEmpty() &&
                node.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/elements_video_title_container") != null &&
                node.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/elements_top_channel_bar_container") != null
    }

    // Helper function to detect Snapchat Spotlight
    private fun detectSnapchatSpotlight(node: AccessibilityNodeInfo): Boolean {
        val spotlightView = node.findAccessibilityNodeInfosByViewId("com.snapchat.android:id/spotlight_view_count")
        return !spotlightView.isNullOrEmpty()
    }

    private fun blockContent() {
        // Perform action to block the content
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    override fun onInterrupt() {
        // Handle interruptions
    }
}

