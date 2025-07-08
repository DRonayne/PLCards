package com.darach.plcards.util

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsHelper @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) {

    /**
     * Track when a user views a screen
     */
    fun trackScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    /**
     * Track when a user views a card
     */
    fun trackCardViewed(cardId: String, cardName: String) {
        val bundle = Bundle().apply {
            putString("card_id", cardId)
            putString("card_name", cardName)
        }
        firebaseAnalytics.logEvent("card_viewed", bundle)
    }

    /**
     * Track when a user performs a search
     */
    fun trackSearchPerformed(query: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SEARCH_TERM, query)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, bundle)
    }

    /**
     * Track when a user toggles favorite status on a card
     */
    fun trackFavoriteToggled(cardId: String, isFavorite: Boolean) {
        val bundle = Bundle().apply {
            putString("card_id", cardId)
            putBoolean("is_favorite", isFavorite)
        }
        firebaseAnalytics.logEvent("favorite_toggled", bundle)
    }

    /**
     * Track when a user shares content
     */
    fun trackShare(shareType: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, shareType)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle)
    }

    /**
     * Track when a user navigates to a specific shelf/grid view
     */
    fun trackShelfNavigation(shelfType: String) {
        val bundle = Bundle().apply {
            putString("shelf_type", shelfType)
        }
        firebaseAnalytics.logEvent("shelf_navigation", bundle)
    }

    /**
     * Track when a user changes settings
     */
    fun trackSettingChanged(settingName: String, settingValue: String) {
        val bundle = Bundle().apply {
            putString("setting_name", settingName)
            putString("setting_value", settingValue)
        }
        firebaseAnalytics.logEvent("setting_changed", bundle)
    }

    /**
     * Track when WC2002 mode is activated/deactivated
     */
    fun trackWc2002ModeToggled(isEnabled: Boolean) {
        val bundle = Bundle().apply {
            putBoolean("wc2002_mode_enabled", isEnabled)
        }
        firebaseAnalytics.logEvent("wc2002_mode_toggled", bundle)
    }

    /**
     * Track when a user views their My XI formation
     */
    fun trackMyXIViewed() {
        firebaseAnalytics.logEvent("my_xi_viewed", Bundle())
    }

    /**
     * Track when a user updates their My XI formation
     */
    fun trackMyXIUpdated() {
        firebaseAnalytics.logEvent("my_xi_updated", Bundle())
    }
}