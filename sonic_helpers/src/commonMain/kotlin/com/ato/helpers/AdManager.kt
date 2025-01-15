package com.ato.helpers

import androidx.compose.runtime.Composable

interface AdManager {
    fun loadAd(adUnitId: String)
    fun showAd()
    @Composable
    fun AdMobBanner()
}