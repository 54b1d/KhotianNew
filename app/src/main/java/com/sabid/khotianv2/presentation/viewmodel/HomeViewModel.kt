package com.sabid.khotianv2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sabid.khotianv2.domain.model.HomeData
import com.sabid.khotianv2.domain.usecase.GetHomeDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeDataUseCase: GetHomeDataUseCase
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val homeData: StateFlow<HomeData?> = _selectedDate
        .flatMapLatest { date ->
            getHomeDataUseCase(date)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
    }

    fun onPreviousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }

    fun onNextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
    }
}
