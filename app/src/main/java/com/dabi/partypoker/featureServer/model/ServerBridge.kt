package com.dabi.partypoker.featureServer.model

import android.util.Log
import com.dabi.partypoker.managers.ServerEvents
import com.dabi.partypoker.managers.ServerManager
import com.dabi.partypoker.managers.ServerStatusEnum
import com.dabi.partypoker.featureServer.model.data.ServerState
import com.dabi.partypoker.utils.ClientPayloadType
import com.dabi.partypoker.utils.ServerPayloadType
import com.dabi.partypoker.utils.fromClientPayload
import com.dabi.partypoker.utils.toServerPayload
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Payload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update


sealed class ServerBridgeEvents{
//    data class EstablishConnection(val endpointID: String, val nickname: String): ServerBridgeEvents()
    data class ClientDisconnected(val endpointID: String): ServerBridgeEvents()

    data class ClientAction(val endpointID: String, val action: ClientPayloadType, val data: Any?): ServerBridgeEvents()
}


class ServerBridge(
    private val connectionsClient: ConnectionsClient,
    private val bridgeEvent: (ServerBridgeEvents) -> Unit
) {
    private val _serverState = MutableStateFlow(ServerState())
    val serverState: StateFlow<ServerState> = _serverState

    fun killServer(){
        connectionsClient.stopAdvertising()
        connectionsClient.stopAllEndpoints()
    }

    fun leave(){
        _serverState.update { it.copy(
            serverStatus = ServerStatusEnum.OFF
        ) }
    }

    fun onServerEvent(event: ServerEvents){
        when(event) {
            is ServerEvents.StartServer -> {
                if (_serverState.value.serverStatus == ServerStatusEnum.NONE ||
                    _serverState.value.serverStatus == ServerStatusEnum.ADVERTISING_FAILED
                ){
                    _serverState.update {
                        it.copy(
                            serverStatus = ServerStatusEnum.NONE
                        )
                    }
                    ServerManager(connectionsClient, this::onServerEvent).startAdvertising(
                        context = event.context,
                        name = event.serverName
                    )

                    _serverState.update { it.copy(
                        serverType = event.serverType
                    ) }
                }
            }
            is ServerEvents.ServerStatus -> {
                _serverState.update { it.copy(
                    serverStatus = event.status
                ) }
            }

            is ServerEvents.ClientConnected -> {
                val clientEndpointId = event.endpointId

                if (_serverState.value.connectedClients.size >= 10){
                    Log.e("", "TABLE IS FULL")

                    val serverPayload = toServerPayload(ServerPayloadType.ROOM_IS_FULL, null)
                    connectionsClient.sendPayload(clientEndpointId, serverPayload)

                    // TODO: Remove player from game
                    return
                }

                _serverState.update { it.copy(
                    connectedClients = it.connectedClients.plus(clientEndpointId)
                )}

                val serverPayload = toServerPayload(ServerPayloadType.CLIENT_CONNECTED, _serverState.value.serverType)
                connectionsClient.sendPayload(clientEndpointId, serverPayload)
            }
            is ServerEvents.ClientDisconnected -> {
                val clientEndpointId = event.endpointId

                _serverState.update { it.copy(
                    connectedClients = it.connectedClients.filter { clients -> clients != clientEndpointId }
                ) }

                bridgeEvent(ServerBridgeEvents.ClientDisconnected(clientEndpointId))
            }

            is ServerEvents.PayloadReceived -> {
                val clientID = event.receivedFrom
                val result: Pair<ClientPayloadType, Any?> = fromClientPayload(event.payload)

                val clientPayloadType = result.first
                val data = result.second

                bridgeEvent(ServerBridgeEvents.ClientAction(clientID, clientPayloadType, data))
            }
        }
    }

    fun sendPayload(clientID: String, serverPayload: Payload){
        if (_serverState.value.connectedClients.contains(clientID)){
            connectionsClient.sendPayload(
                clientID,
                serverPayload
            )
        }
    }
    fun sendPayload(serverPayload: Payload){
        if (_serverState.value.connectedClients.isNotEmpty()){
            connectionsClient.sendPayload(_serverState.value.connectedClients, serverPayload)
        }
    }

    fun stopAdvertising() {
        connectionsClient.stopAdvertising()
        _serverState.update { it.copy(
            serverStatus = ServerStatusEnum.ACTIVE
        )}
    }
}
