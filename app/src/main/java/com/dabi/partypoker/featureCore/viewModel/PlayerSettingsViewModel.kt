package com.dabi.partypoker.featureCore.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.partypoker.featureCore.data.PlayerSettingsState
import com.dabi.partypoker.featureCore.repository.PlayerSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class PlayerSettingsEvent(){
    data object ToggleVibration: PlayerSettingsEvent()
}

@HiltViewModel
class PlayerSettingsViewModel @Inject constructor (
    private val playerSettingsRepository: PlayerSettingsRepository
): ViewModel() {
    val playerSettingsState: StateFlow<PlayerSettingsState> = playerSettingsRepository.currentSettings.map {
        it.copy()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerSettingsState()
    )

    fun onEvent(event: PlayerSettingsEvent) {
        when (event) {
            is PlayerSettingsEvent.ToggleVibration -> {
                toggleVibration()
            }
        }
    }

    private fun toggleVibration() {
        viewModelScope.launch {
            playerSettingsRepository.toggleVibration()
        }
    }
}