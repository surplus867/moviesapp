package com.minyu.moviesapp.core.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.minyu.moviesapp.R
import com.minyu.moviesapp.core.LanguagePrefs
import com.minyu.moviesapp.core.MainActivity

/**
 * Activity that lets the user pick an app language, persists the choice,
 * applies it and then relaunches MainActivity so UI strings reload.
 */
class LanguageSelectionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If there is a saved language, apply it so this screen's strings are localize too.
        // Wrap it in try/catch to avoid crashing if locale application fails.
        try {
            LanguagePrefs.applyAppLocale(this)
        } catch (t: Throwable) {
            // Fail-safe: lof the throwable but keep the activity running.
            t.printStackTrace()
        }

        setContent {
            // Composable content that shows the language list.
            LanguageSelectionScreen { languageCode ->
                // Persist + apply per-app locales (no deprecated APIs).
                // Use try/catch to be defective around persistence/ apply code.
                try {
                    LanguagePrefs.setAndApply(this, languageCode)
                } catch (t: Throwable) {
                    t.printStackTrace()
                }

                // Relaunch Main so strings reload in the new locale.
                startActivity(
                    Intent(this, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
                finish() // Close the activity after launching main.
            }
        }
    }
}

/**
 * Simple language selection UI Compose.
 *
 * Notes:
 * - 'language is a small list of (languageCode, displayName) pairs.
 * - Each language line is clickable; 'indication = null 'disables the ripple
 *  as a temporary workaround for mixed Compose dependency issues (PlatformRipple
 *  vs. IndicationNodeFactory mismatch). Keep 'interactionSource' for semantics.
 */

@Composable
fun LanguageSelectionScreen(onLanguageSelected: (String) -> Unit) {
    // Language offered to the user: pair of locale code and display name.
    val languages = listOf(
        "en" to "English",
        "zh-HK" to "繁體中文（香港）",
        "ko" to "한국어",
        "ja" to "日本語"
    )

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        // App logo image
        Image(
            painter = painterResource(id = R.drawable.movie_logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(80.dp)
        )

        Spacer(Modifier.height(24.dp))

        // Title text (localized via string resources)
        Text(stringResource(id = R.string.selected_language), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        // Render each language as a full-width Text that is clickable.
        languages.forEach { (code, name) ->
            Text(
                text = name,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current
                    ) { onLanguageSelected(code) }
                    .padding(8.dp)
            )
        }
    }
}