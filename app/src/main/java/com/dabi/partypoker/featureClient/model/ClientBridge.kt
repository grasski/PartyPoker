package com.dabi.partypoker.featureClient.model

import com.dabi.partypoker.managers.ClientEvents
import com.dabi.partypoker.managers.ClientManager
import com.dabi.partypoker.managers.ConnectionStatusEnum
import com.dabi.partypoker.managers.ServerType
import com.dabi.partypoker.featureClient.model.data.ClientState
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.utils.ServerPayloadType
import com.dabi.partypoker.utils.fromServerPayload
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Payload
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


sealed class ClientBridgeEvents{
    data class Connect(val nickname: String): ClientBridgeEvents()
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

    fun onClientEvent(event: ClientEvents){
        when(event){
            is ClientEvents.Connect -> {
                ClientManager(connectionsClient, this::onClientEvent).startDiscovery(event.context, event.nickname)
                bridgeEvent(ClientBridgeEvents.Connect(event.nickname))
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
                    ServerPayloadType.UPDATE_CLIENT -> {
                        val playerState = Gson().fromJson(data.toString(), PlayerState::class.java)
                        bridgeEvent(ClientBridgeEvents.UpdateClient(playerState))
                    }
                    ServerPayloadType.UPDATE_GAME_STATE -> {
                        val gameState = Gson().fromJson(data.toString(), GameState::class.java)
                        bridgeEvent(ClientBridgeEvents.UpdateGameState(gameState))
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