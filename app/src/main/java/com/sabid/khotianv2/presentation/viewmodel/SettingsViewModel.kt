package com.sabid.khotianv2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sabid.khotianv2.domain.model.CommaStyle
import com.sabid.khotianv2.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val commaStyle: StateFlow<CommaStyle> = settingsRepository.getCommaStyle()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CommaStyle.BD)

    fun setCommaStyle(style: CommaStyle) {
        viewModelScope.launch {
            settingsRepository.setCommaStyle(style)
        }
    }
}
