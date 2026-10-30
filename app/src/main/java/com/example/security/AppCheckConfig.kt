package com.example.security

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory
import java.util.UUID

private const val TAG = "AppCheckInit"

// App Check contract keys defined by the Firebase SDK and runtime harness. Do not rename.
private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

fun configureAppCheck(context: Context, intent: Intent?) {
    try {
        // 1. Ensure FirebaseApp is initialized
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }

        if (BuildConfig.DEBUG) {
            // 2. Debug: Read debug token from Intent extra (provided in AI Studio / test harness)
            val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)?.trim()
            val persistenceKey = FirebaseApp.getInstance().persistenceKey
            val prefs = context.getSharedPreferences(
                "$DEBUG_STORE_PREFIX$persistenceKey",
                Context.MODE_PRIVATE
            )

            if (!debugToken.isNullOrEmpty()) {
                // CRITICAL: MUST use commit() instead of apply() so the secret is written
                // synchronously before DebugAppCheckProviderFactory reads it.
                prefs.edit()
                    .putString(DEBUG_SECRET_KEY, debugToken)
                    .commit()
                Log.d(TAG, "App Check debug secret configured from Intent extra.")
            } else {
                // When launched on a physical phone from the launcher icon, no Intent extra is
                // present. Ensure a persistent UUID debug secret exists synchronously via commit()
                // so DebugAppCheckProviderFactory uses it and the user can view/copy it in the UI
                // without needing a PC or adb logcat.
                val existingSecret = prefs.getString(DEBUG_SECRET_KEY, null)?.trim()
                if (existingSecret.isNullOrEmpty()) {
                    val generatedSecret = UUID.randomUUID().toString()
                    prefs.edit()
                        .putString(DEBUG_SECRET_KEY, generatedSecret)
                        .commit()
                    Log.d(TAG, "Generated persistent App Check debug secret for device: $generatedSecret")
                } else {
                    Log.d(TAG, "Using existing App Check debug secret from SharedPreferences: $existingSecret")
                }
            }

            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            Log.d(TAG, "DebugAppCheckProviderFactory installed.")
        } else {
            // 3. Release: Look up recaptcha_site_key safely without throwing Resources.NotFoundException
            val resId = context.resources.getIdentifier(
                RECAPTCHA_SITE_KEY_RES,
                "string",
                context.packageName
            ).let { id ->
                if (id != 0) id else context.resources.getIdentifier(RECAPTCHA_SITE_KEY_RES, "string", "com.example")
            }
            if (resId != 0) {
                val siteKey = context.getString(resId)
                if (siteKey.isNotBlank()) {
                    FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                        RecaptchaAppCheckProviderFactory.getInstance(siteKey)
                    )
                    Log.d(TAG, "RecaptchaAppCheckProviderFactory installed.")
                } else {
                    Log.e(TAG, "recaptcha_site_key resource string is blank.")
                }
            } else {
                Log.e(TAG, "recaptcha_site_key string resource not found in package.")
            }
        }
    } catch (e: Exception) {
        // Log explicitly to keep App Check attestation failures visible
        Log.e(TAG, "Failed to initialize Firebase App Check", e)
    }
}

/**
 * Returns the active App Check debug token stored in the Firebase DebugAppCheckProvider
 * SharedPreferences store so the user can copy it directly on their phone and register it in
 * Firebase Console -> App Check -> Apps -> Manage debug tokens (no PC or logcat required).
 */
fun getActiveAppCheckDebugToken(context: Context): String {
    return try {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        val persistenceKey = FirebaseApp.getInstance().persistenceKey
        val prefs = context.getSharedPreferences(
            "$DEBUG_STORE_PREFIX$persistenceKey",
            Context.MODE_PRIVATE
        )
        val current = prefs.getString(DEBUG_SECRET_KEY, null)?.trim()
        if (!current.isNullOrEmpty()) {
            current
        } else {
            val generated = UUID.randomUUID().toString()
            prefs.edit().putString(DEBUG_SECRET_KEY, generated).commit()
            generated
        }
    } catch (e: Exception) {
        Log.e(TAG, "Unable to read App Check debug token", e)
        ""
    }
}

/**
 * Allows the user to paste a pre-registered Debug Token (UUID) from Firebase Console directly
 * in the app on their phone. Writes synchronously with .commit() and reinstalls
 * DebugAppCheckProviderFactory.
 */
fun saveCustomAppCheckDebugToken(context: Context, customToken: String): Boolean {
    val cleanToken = customToken.trim()
    if (cleanToken.isEmpty()) return false
    return try {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        val persistenceKey = FirebaseApp.getInstance().persistenceKey
        val prefs = context.getSharedPreferences(
            "$DEBUG_STORE_PREFIX$persistenceKey",
            Context.MODE_PRIVATE
        )
        val saved = prefs.edit()
            .putString(DEBUG_SECRET_KEY, cleanToken)
            .commit()
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )
        Log.d(TAG, "Updated custom App Check debug token and reinstalled DebugAppCheckProviderFactory.")
        saved
    } catch (e: Exception) {
        Log.e(TAG, "Failed to save custom App Check debug token", e)
        false
    }
}
