package com.minyu.moviesapp.details.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minyu.moviesapp.core.data.TranslateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsTranslateViewModel @Inject constructor(
    private val translateRepo: TranslateRepository
) : ViewModel() {

    private val _translatedPlot = MutableStateFlow<String?>(null)
    val translatedPlot: StateFlow<String?> = _translatedPlot.asStateFlow()

    fun translatePlot(original: String, sourceLang: String = "en", targetLang: String) {
        if (original.isBlank() || sourceLang == targetLang) {
            _translatedPlot.value = original
            return
        }
        viewModelScope.launch {
            _translatedPlot.value = try {
                translateRepo.translate(original,targetLang)
            } catch (t: Throwable) {
                original
            }
        }
    }
}