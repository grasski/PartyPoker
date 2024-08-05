package com.dabi.partypoker.featureMenu.viewModel

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


sealed class MenuGameSettingsEvent{
    class SelectSettings(val setting: GameSettings): MenuGameSettingsEvent()
    class DeleteSettings(val setting: GameSettings): MenuGameSettingsEvent()
}

sealed class NewSettingsEvent{
    data object Reset: NewSettingsEvent()
    class ChangeUpdateToNew(val setting: GameSettings): NewSettingsEvent()
    data object SaveSettings: NewSettingsEvent()

    class ChangeTitle(val title: String): NewSettingsEvent()
    class ChangePlayerMoney(val money: Int?): NewSettingsEvent()
    class ChangeSmallBlindAmount(val amount: Int?): NewSettingsEvent()
    class ChangeBigBlindAmount(val amount: Int?): NewSettingsEvent()
    class ChangePlayerTimeout(val timeout: Int?): NewSettingsEvent()
    class ChangeGameOverTimeout(val timeout: Int?): NewSettingsEvent()
}

enum class SettingsError {
    EMPTY_TITLE,
    INVALID_PLAYER_MONEY,
    INVALID_SMALL_BLIND,
    INVALID_BIG_BLIND,
    SMALL_BLIND_GREATER_THAN_BIG_BLIND,
    BIG_BLIND_GREATER_THAN_PLAYER_MONEY,
    INVALID_PLAYER_TIMER,
    INVALID_GAME_OVER_TIMER
}

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val db: GameSettingsDatabase
): ViewModel() {
    private val _gameSettings = MutableStateFlow(emptyList<GameSettings>())
    val gameSettings = _gameSettings.asStateFlow()

    private val _selectedSetting = MutableStateFlow(GameSettings())
    val selectedSetting = _selectedSetting.asStateFlow()

    private val _newSettings = MutableStateFlow(GameSettings(title="", isDefault = false))
    val newSettings = _newSettings.asStateFlow()
    private val _errors = MutableStateFlow<List<SettingsError>>(emptyList())
    val errors = _errors.asStateFlow()

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

    private fun validateSettings(settings: GameSettings): List<SettingsError> {
        val errors = mutableListOf<SettingsError>()
        if (settings.title.isEmpty()) errors.add(SettingsError.EMPTY_TITLE)
        if (settings.playerMoney <= 0) errors.add(SettingsError.INVALID_PLAYER_MONEY)
        if (settings.smallBlindAmount <= 0) errors.add(SettingsError.INVALID_SMALL_BLIND)
        if (settings.bigBlindAmount <= 0) errors.add(SettingsError.INVALID_BIG_BLIND)
        if (settings.smallBlindAmount > settings.bigBlindAmount) errors.add(SettingsError.SMALL_BLIND_GREATER_THAN_BIG_BLIND)
        if (settings.bigBlindAmount > settings.playerMoney) errors.add(SettingsError.BIG_BLIND_GREATER_THAN_PLAYER_MONEY)
        if (settings.playerTimerDurationMillis <= 0) errors.add(SettingsError.INVALID_PLAYER_TIMER)
        if (settings.gameOverTimerDurationMillis <= 0) errors.add(SettingsError.INVALID_GAME_OVER_TIMER)
        return errors
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
                _errors.update { validateSettings(_newSettings.value) }
            }
            is NewSettingsEvent.ChangeUpdateToNew -> {
                _newSettings.update { event.setting }
                _errors.update { validateSettings(_newSettings.value) }
            }
            is NewSettingsEvent.SaveSettings -> {
                val errors = validateSettings(_newSettings.value)
                if (errors.isNotEmpty()) {
                    _errors.update { errors }
                    return
                }

                viewModelScope.launch {
                    val id = db.dao.upsertSetting(_newSettings.value).takeIf { it != -1L } ?: _newSettings.value.id
                    _selectedSetting.update { _newSettings.value.copy(id = id) }
                    _errors.value = emptyList()
                }
            }

            is NewSettingsEvent.ChangeTitle -> {
                _newSettings.update { it.copy(title = event.title) }
                _errors.update { validateSettings(_newSettings.value) }
            }
            is NewSettingsEvent.ChangePlayerMoney -> {
                event.money?.let {
                    if (event.money > 0){
                        _newSettings.update { it.copy(playerMoney = event.money) }
                        _errors.update { validateSettings(_newSettings.value) }
                        return
                    }
                }
                _newSettings.update { it.copy(playerMoney = 0) }
                _errors.update { validateSettings(_newSettings.value) }
            }
            is NewSettingsEvent.ChangeSmallBlindAmount -> {
                event.amount?.let {
                    if (event.amount > 0) {
                        _newSettings.update { it.copy(smallBlindAmount = event.amount) }
                        _errors.update { validateSettings(_newSettings.value) }
                        return
                    }
                }
                _newSettings.update { it.copy(smallBlindAmount = 0) }
                _errors.update { validateSettings(_newSettings.value) }
            }
            is NewSettingsEvent.ChangeBigBlindAmount -> {
                event.amount?.let {
                    if (event.amount > 0) {
                        _newSettings.update { it.copy(bigBlindAmount = event.amount) }
                        _errors.update { validateSettings(_newSettings.value) }
                        return
                    }
                }
                _newSettings.update { it.copy(bigBlindAmount = 0) }
                _errors.update { validateSettings(_newSettings.value) }
            }
            is NewSettingsEvent.ChangePlayerTimeout -> {
                event.timeout?.let {
                    if (event.timeout > 0) {
                        _newSettings.update { it.copy(playerTimerDurationMillis = (event.timeout * 1000)) }
                        _errors.update { validateSettings(_newSettings.value) }
                        return
                    }
                }
                _newSettings.update { it.copy(playerTimerDurationMillis = 0) }
                _errors.update { validateSettings(_newSettings.value) }
            }
            is NewSettingsEvent.ChangeGameOverTimeout -> {
                event.timeout?.let {
                    if (event.timeout > 0) {
                        _newSettings.update { it.copy(gameOverTimerDurationMillis = (event.timeout * 1000)) }
                        _errors.update { validateSettings(_newSettings.value) }
                        return
                    }
                }
                _newSettings.update { it.copy(gameOverTimerDurationMillis = 0) }
                _errors.update { validateSettings(_newSettings.value) }
            }
        }
    }
}