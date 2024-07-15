package com.dabi.partypoker.featureMenu.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.partypoker.repository.gameSettings.GameSettings
import com.dabi.partypoker.repository.gameSettings.GameSettingsDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt


sealed class MenuGameSettingsEvent{
    class SelectSettings(val setting: GameSettings): MenuGameSettingsEvent()
    class DeleteSettings(val setting: GameSettings): MenuGameSettingsEvent()
}

sealed class NewSettingsEvent{
    data object Reset: NewSettingsEvent()
    class ChangeUpdateToNew(val setting: GameSettings): NewSettingsEvent()
//    class SaveSettings(val setting: GameSettings): NewSettingsEvent()
    data object SaveSettings: NewSettingsEvent()


    class ChangeTitle(val title: String): NewSettingsEvent()
    class ChangePlayerMoney(val money: Int?): NewSettingsEvent()
    class ChangeSmallBlindAmount(val amount: Int?): NewSettingsEvent()
    class ChangeBigBlindAmount(val amount: Int?): NewSettingsEvent()
    class ChangePlayerTimeout(val timeout: Float?): NewSettingsEvent()
    class ChangeGameOverTimeout(val timeout: Float?): NewSettingsEvent()
}


@HiltViewModel
class MenuViewModel @Inject constructor(
    private val db: GameSettingsDatabase
): ViewModel() {
    private val _gameSettings = MutableStateFlow(emptyList<GameSettings>())
    val gameSettings = _gameSettings.asStateFlow()

    private val _selectedSetting = MutableStateFlow(GameSettings())
    val selectedSetting = _selectedSetting.asStateFlow()

    private val _newSettings = MutableStateFlow(GameSettings(title=""))
    val newSettings = _newSettings.asStateFlow()

    init {
        viewModelScope.launch {
            db.dao.getAllSettings().collect{ settings ->
                if (settings.isEmpty()){
                    val gs = GameSettings()
                    val id = db.dao.upsertSetting(gs)
                    _selectedSetting.update { gs.copy(id = id) }
                    return@collect
                }

                _gameSettings.update { settings }
            }
        }
    }

    fun onEvent(event: MenuGameSettingsEvent) {
        when (event) {
            is MenuGameSettingsEvent.SelectSettings -> {
                _selectedSetting.update { event.setting }
            }
            is MenuGameSettingsEvent.DeleteSettings -> {
                viewModelScope.launch {
                    db.dao.deleteSetting(event.setting)

                    _selectedSetting.update { _gameSettings.value.first() }
                }
            }
        }
    }

    fun onChangeEvent(event: NewSettingsEvent) {
        when(event){
            is NewSettingsEvent.Reset -> {
                _newSettings.update { GameSettings(title="", isDefault = false) }
            }
            is NewSettingsEvent.ChangeUpdateToNew -> {
                _newSettings.update { event.setting }
            }
            is NewSettingsEvent.SaveSettings -> {
                if (_newSettings.value.title.isEmpty()){
                    return
                }

                viewModelScope.launch {
                    val id = db.dao.upsertSetting(_newSettings.value).takeIf { it != -1L } ?: _newSettings.value.id
                    _selectedSetting.update { _newSettings.value.copy(id = id) }
                }
            }

            is NewSettingsEvent.ChangeTitle -> {
                _newSettings.update { it.copy(title = event.title) }
            }
            is NewSettingsEvent.ChangePlayerMoney -> {
                event.money?.let {
                    if (event.money > 0){
                        _newSettings.update { it.copy(playerMoney = event.money) }
                        return
                    }
                }
                _newSettings.update { it.copy(playerMoney = 1) }
            }
            is NewSettingsEvent.ChangeSmallBlindAmount -> {
                event.amount?.let {
                    if (event.amount > 0) {
                        _newSettings.update { it.copy(smallBlindAmount = event.amount) }
                        return
                    }
                }
                _newSettings.update { it.copy(smallBlindAmount = 1) }
            }
            is NewSettingsEvent.ChangeBigBlindAmount -> {
                event.amount?.let {
                    if (event.amount > 0) {
                        _newSettings.update { it.copy(bigBlindAmount = event.amount) }
                        return
                    }
                }
                _newSettings.update { it.copy(bigBlindAmount = 1) }
            }
            is NewSettingsEvent.ChangePlayerTimeout -> {
                event.timeout?.let {
                    if (event.timeout > 0) {
                        _newSettings.update { it.copy(playerTimerDurationMillis = (event.timeout * 1000).roundToInt()) }
                        return
                    }
                }
                _newSettings.update { it.copy(playerTimerDurationMillis = 1000) }
            }
            is NewSettingsEvent.ChangeGameOverTimeout -> {
                event.timeout?.let {
                    if (event.timeout > 0) {
                        _newSettings.update { it.copy(gameOverTimerDurationMillis = (event.timeout * 1000).roundToInt()) }
                        return
                    }
                }
                _newSettings.update { it.copy(gameOverTimerDurationMillis = 1000) }
            }
        }
    }
}