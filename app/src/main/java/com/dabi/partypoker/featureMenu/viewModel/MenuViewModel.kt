package com.dabi.partypoker.featureMenu.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.partypoker.repository.gameSettings.GameSettings
import com.dabi.partypoker.repository.gameSettings.GameSettingsDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class MenuGameSettingsEvent{
    class SelectSettings(val setting: GameSettings): MenuGameSettingsEvent()
}


@HiltViewModel
class MenuViewModel @Inject constructor(
    private val db: GameSettingsDatabase
): ViewModel() {
    private val _gameSettings = MutableStateFlow(listOf(GameSettings()))
    val gameSettings = _gameSettings.asStateFlow()

    private val _selectedSetting = MutableStateFlow(GameSettings())
    val selectedSetting = _selectedSetting.asStateFlow()

    init {
        viewModelScope.launch {
            db.dao.getAllSettings().collect{ settings ->
                _gameSettings.update { settings }
            }
            _selectedSetting.update { _gameSettings.value.first() }
        }
    }

    fun onEvent(event: MenuGameSettingsEvent) {
        when (event) {
            is MenuGameSettingsEvent.SelectSettings -> {
                _selectedSetting.update { event.setting }
            }
        }
    }
}