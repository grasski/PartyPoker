package com.dabi.partypoker.featureCore.views

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dabi.partypoker.R
import com.dabi.partypoker.featureCore.data.PlayerSettingsState
import com.dabi.partypoker.featureCore.viewModel.PlayerSettingsEvent
import com.dabi.partypoker.featureCore.viewModel.PlayerSettingsViewModel
import com.dabi.partypoker.utils.UiTexts


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlayerSettings(
    isVisible: Boolean,
    showSettings: (Boolean) -> Unit,
) {
    val playerSettingsVM = hiltViewModel<PlayerSettingsViewModel>()
    val playerSettingsState by playerSettingsVM.playerSettingsState.collectAsStateWithLifecycle()

    val orientation = LocalConfiguration.current.orientation
    Crossfade(
        targetState = isVisible,
        modifier = Modifier
            .zIndex(1f)
    ) {
        if (it) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray.copy(0.6f))
                    .clickable(
                        interactionSource = null,
                        indication = null
                    ) {
                        showSettings(false)
                    }
            ){
                BackHandler(true) {
                    showSettings(false)
                }
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f)
    ){
        AnimatedVisibility(
            visible = isVisible,
            modifier = Modifier
                .align(Alignment.BottomEnd),
            enter = if (orientation == Configuration.ORIENTATION_PORTRAIT) slideInVertically { it } else slideInHorizontally { it },
            exit = if (orientation == Configuration.ORIENTATION_PORTRAIT) slideOutVertically { it } else slideOutHorizontally { it }
        ) {
            LazyColumn(modifier = Modifier
                .fillMaxHeight(if (orientation == Configuration.ORIENTATION_PORTRAIT) 0.5f else 1f)
                .fillMaxWidth(if (orientation == Configuration.ORIENTATION_PORTRAIT) 1f else 0.5f)
                .clip(
                    RoundedCornerShape(
                        topStart = 10.dp,
                        topEnd = if (orientation == Configuration.ORIENTATION_PORTRAIT) 10.dp else 0.dp,
                        bottomEnd = 0.dp,
                        bottomStart = if (orientation == Configuration.ORIENTATION_PORTRAIT) 0.dp else 10.dp
                    )
                )
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(8.dp),
            ){
                stickyHeader {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ){
                        AutoSizeText(
                            text = UiTexts.StringResource(R.string.player_settings).asString(),
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                }

                item {
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Column(
                            modifier = Modifier
                                .weight(1f, false)
                        ) {
                            val texts = UiTexts.ArrayResource(R.array.vibration).asArray()
                            AutoSizeText(
                                text = texts[0],
                                style = MaterialTheme.typography.headlineLarge
                            )
                            AutoSizeText(
                                text = texts[1],
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Switch(
                            checked = playerSettingsState.vibration,
                            onCheckedChange = { playerSettingsVM.onEvent(PlayerSettingsEvent.ToggleVibration) }
                        )
                    }
                }
            }
        }
    }
}