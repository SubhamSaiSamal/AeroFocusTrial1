package com.aerofocus.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point for Hilt dependency injection.
 *
 * Declared in AndroidManifest.xml as `android:name=".AeroFocusApp"`.
 * Hilt uses this to generate the top-level DI component graph.
 */
@HiltAndroidApp
class AeroFocusApp : Application()
