package com.minyu.moviesapp.core.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.minyu.moviesapp.R
import com.minyu.moviesapp.core.LanguagePrefs
import com.minyu.moviesapp.core.MainActivity

/**
 * Lets the user pick a language; persists + applies, then relaunches MainActivity.
 */
class LanguageSelectionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If there is a saved language, apply it so this screen is localized too
        LanguagePrefs.applyAppLocale(this)

        setContent {
            LanguageSelectionScreen { languageCode ->
                // Persist + apply per-app locales (no deprecated APIs)
                LanguagePrefs.setAndApply(this, languageCode)

                // Relaunch Main so strings reload in the new locale
                startActivity(
                    Intent(this, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
                finish()
            }
        }
    }
}

@Composable
fun LanguageSelectionScreen(onLanguageSelected: (String) -> Unit) {
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

        Image(
            painter = painterResource(id = R.drawable.movie_logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(80.dp)
        )

        Spacer(Modifier.height(24.dp))
        Text(stringResource(id = R.string.selected_language), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        languages.forEach { (code, name) ->
            Text(
                text = name,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLanguageSelected(code) }
                    .padding(8.dp)
            )
        }
    }
}