package com.dabi.partypoker.featureClient.view

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dabi.partypoker.featureClient.viewmodel.PlayerEvents
import com.dabi.partypoker.featureClient.viewmodel.PlayerViewModel


@Composable
fun PlayerGameView() {
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val playerState by playerViewModel.playerState.collectAsStateWithLifecycle()
    val clientState by playerViewModel.clientBridge.clientState.collectAsStateWithLifecycle()

    val gameState by playerViewModel.gameState.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = gameState) {}

    Column {
        Text("Player Name: ${playerState.nickname} and ID: ${playerState.id}")
        Text("Server: ${clientState.serverID}, status: ${clientState.connectionStatus}")

        Button(onClick = {
            playerViewModel.onPlayerEvent(PlayerEvents.Disconnect)
        }) {
            Text(text = "DISCONNECT")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "READY = ${playerState.isReadyToPlay}, money = ${playerState.money}")
        Button(onClick = {
            playerViewModel.onPlayerEvent(PlayerEvents.Ready)
        }){
            Text(text = "READY")
        }

        Text(text = "PLAYING = ${playerState.isPlayingNow}, called = ${playerState.called}")
        Text("ROUND = ${gameState.round}, raiser: ${gameState.activeRaise}\nBANK = ${gameState.bank}")

        Row (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    playerViewModel.onPlayerEvent(PlayerEvents.Check)
                },
                enabled = playerViewModel.checkEnabled()
            ) {
                Text(text = "CHECK")
            }

            val callAmount = playerViewModel.activeCallValue()
            Button(
                onClick = {
                    playerViewModel.onPlayerEvent(PlayerEvents.Call(callAmount))
                },
                enabled = callAmount > 0
            ) {
                Text(text = "CALL $callAmount")
            }

            val raisingAmount = 30
            Button(
                onClick = {
                    // So that is at least 30 + smallBlindValue
                    playerViewModel.onPlayerEvent(PlayerEvents.Raise(playerViewModel.minimalRaise() + raisingAmount))
                },
                enabled = playerState.isPlayingNow
            ) {
                Text(text = "min RAISE ${playerViewModel.minimalRaise()}\nRaising: ${playerViewModel.minimalRaise() + raisingAmount}")
            }

            Button(
                onClick = {
                    playerViewModel.onPlayerEvent(PlayerEvents.Fold)
                },
                enabled = playerState.isPlayingNow
            ) {
                Text(text = "FOLD")
            }
        }
    }
}