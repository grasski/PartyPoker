package com.dabi.partypoker.featureClient.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureClient.viewmodel.PlayerEvents
import com.dabi.partypoker.featureCore.data.PlayerActionsState


@Composable
fun PlayerViewPrivate(
    navController: NavController,
    playerState: PlayerState,
    playerActionsState: PlayerActionsState,
    onPlayerEvent: (PlayerEvents) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column {
            IconButton(onClick = {
//                showPopUpMenu = true
                // TODO()
            }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }

            Text(text = "Connected to the server!")

            Button(onClick = {
                onPlayerEvent(PlayerEvents.Ready)
            }) {
                Text(text = "READY")
            }

            Button(onClick = {
                onPlayerEvent(PlayerEvents.Leave)
            }) {
                Text(text = "Disconnect")
            }


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        onPlayerEvent(PlayerEvents.Check)
                    },
                    enabled = playerActionsState.canCheck
                ) {
                    Text(text = "CHECK")
                }
                val callAmount = playerActionsState.callAmount
                Button(
                    onClick = {
                        onPlayerEvent(PlayerEvents.Call(callAmount))
                    },
                    enabled = callAmount > 0
                ) {
                    Text(text = "CALL $callAmount")
                }
                val raisingAmount = 30
                Button(
                    onClick = {
                        // So that is at least 30 + bigBlindValue
                        onPlayerEvent(PlayerEvents.Raise(playerActionsState.raiseAmount + raisingAmount))
                    },
                    enabled = playerState.isPlayingNow
                ) {
                    Text(text = "min RAISE ${playerActionsState.raiseAmount}\nRaising: ${playerActionsState.raiseAmount + raisingAmount}")
                }
                Button(
                    onClick = {
                        onPlayerEvent(PlayerEvents.Fold)
                    },
                    enabled = playerActionsState.canFold
                ) {
                    Text(text = "FOLD")
                }
            }
        }
    }
}