package com.dabi.partypoker.featureServer.model

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
import kotlinx.coroutines.flow.asStateFlow
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
    val serverState: StateFlow<ServerState> = _serverState.asStateFlow()


    fun onServerEvent(event: ServerEvents){
        when(event) {
            is ServerEvents.StartServer -> {
                if (_serverState.value.serverStatus == ServerStatusEnum.OFF){
                    ServerManager(connectionsClient, this::onServerEvent).startAdvertising(event.context)

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
}