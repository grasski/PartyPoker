package com.dabi.partypoker.featureClient.model

import android.util.Log
import androidx.annotation.RawRes
import com.dabi.partypoker.managers.ClientEvents
import com.dabi.partypoker.managers.ClientManager
import com.dabi.partypoker.managers.ConnectionStatusEnum
import com.dabi.partypoker.managers.ServerType
import com.dabi.partypoker.featureClient.model.data.ClientState
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.utils.ServerPayloadType
import com.dabi.partypoker.utils.UiTexts
import com.dabi.partypoker.utils.UiTextsAdapter
import com.dabi.partypoker.utils.fromServerPayload
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Payload
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject


sealed class ClientBridgeEvents{
    data class Connect(val nickname: String, @RawRes val avatarId: Int?): ClientBridgeEvents()
    data object ClientConnected: ClientBridgeEvents()

    data class UpdateClient(val playerState: PlayerState): ClientBridgeEvents()
    data class UpdateGameState(val gameState: GameState): ClientBridgeEvents()
}

class ClientBridge (
    private val connectionsClient: ConnectionsClient,
    private val bridgeEvent: (ClientBridgeEvents) -> Unit
){
    private val _clientState = MutableStateFlow(ClientState())
    val clientState = _clientState.asStateFlow()

    fun killClient(){
        connectionsClient.disconnectFromEndpoint(clientState.value.serverID)
        connectionsClient.stopDiscovery()
        connectionsClient.stopAllEndpoints()
    }

    fun leave(){
        _clientState.update { it.copy(
            connectionStatus = ConnectionStatusEnum.LEFT
        ) }
    }

    fun onClientEvent(event: ClientEvents){
        when(event){
            is ClientEvents.Connect -> {
                if (_clientState.value.connectionStatus == ConnectionStatusEnum.NONE || _clientState.value.connectionStatus == ConnectionStatusEnum.FAILED_TO_CONNECT){
                    ClientManager(connectionsClient, this::onClientEvent).startDiscovery(event.context, event.nickname)
                    bridgeEvent(ClientBridgeEvents.Connect(event.nickname, event.avatarId))
                }
            }
            is ClientEvents.Connected -> {
                _clientState.update { it.copy(
                    serverID = event.serverId,
                    connectionStatus = ConnectionStatusEnum.CONNECTED
                ) }
                connectionsClient.stopDiscovery()

                bridgeEvent(ClientBridgeEvents.ClientConnected)
            }
            is ClientEvents.ConnectionStatus -> {
                _clientState.update { it.copy(
                    connectionStatus = event.status
                ) }
            }

            is ClientEvents.PayloadReceived -> {
                val result: Pair<ServerPayloadType, Any> = fromServerPayload(event.payload)

                val serverPayloadType = result.first
                val data = result.second
                when(serverPayloadType){
                    ServerPayloadType.CLIENT_CONNECTED -> {
                        _clientState.update { it.copy(
                            serverType = ServerType.valueOf(result.second.toString()),
                        ) }
                    }
                    ServerPayloadType.ROOM_IS_FULL -> {
                        Log.e("", "ROOM IS FULL")
                    }

                    ServerPayloadType.UPDATE_CLIENT -> {
                        val gson = GsonBuilder()
                            .registerTypeAdapter(UiTexts::class.java, UiTextsAdapter())
                            .create()

                        Log.e("", "TADYYY 1")
                        val playerState = gson.fromJson(gson.toJson(data), PlayerState::class.java)
                        bridgeEvent(ClientBridgeEvents.UpdateClient(playerState))
                        Log.e("", "TADYYY 2")
                    }
                    ServerPayloadType.UPDATE_GAME_STATE -> {
                        val gson = GsonBuilder()
                            .registerTypeAdapter(UiTexts::class.java, UiTextsAdapter())
                            .create()

                        Log.e("", "TADYYY 3")
                        val gameState = gson.fromJson(gson.toJson(data), GameState::class.java)
                        bridgeEvent(ClientBridgeEvents.UpdateGameState(gameState))
                        Log.e("", "TADYYY 4")
                    }
                }
            }
        }
    }

    fun sendPayload(clientPayload: Payload){
        connectionsClient.sendPayload(
            _clientState.value.serverID,
            clientPayload
        )
    }
}