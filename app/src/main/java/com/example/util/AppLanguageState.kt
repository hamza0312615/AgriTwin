package com.example.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Global reactive language state for AgriTwin.
 * Enables instant bilingual switching between English and Urdu across all screens.
 */
object AppLanguageState {
    var isUrdu by mutableStateOf(false)

    fun toggleLanguage() {
        isUrdu = !isUrdu
    }

    // Navigation Labels
    fun navHome(): String = if (isUrdu) "ہوم" else "Home"
    fun navTwin(): String = if (isUrdu) "کلائمیٹ ہمزاد" else "Twin"
    fun navWater(): String = if (isUrdu) "پانی کا تجزیہ" else "Water"
    fun navFarm(): String = if (isUrdu) "میرا فارم" else "Farm"
    fun navAlerts(): String = if (isUrdu) "الرٹس" else "Alerts"

    // Common Strings
    fun appTitle(): String = if (isUrdu) "ایگری ٹوئن" else "AgriTwin"
    fun appSubtitle(): String = if (isUrdu) "زرعی سیٹلائٹ انٹیلیجنس" else "Climate-Twin Farming Intelligence"
    fun offline(): String = if (isUrdu) "آف لائن" else "Offline"
    fun live(): String = if (isUrdu) "لائیو ناسا" else "Live NASA"
    fun listen(): String = if (isUrdu) "آواز میں سنیں" else "Listen"
    fun stopListening(): String = if (isUrdu) "بند کریں" else "Stop"
}
