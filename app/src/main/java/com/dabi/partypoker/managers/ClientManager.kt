package com.dabi.partypoker.managers

import android.content.Context
import android.util.Log
import androidx.annotation.RawRes
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy


sealed class ClientEvents{
    data class Connect(val context: Context, val nickname: String, @RawRes val avatarId: Int?): ClientEvents()
    data class ConnectionStatus(val status: ConnectionStatusEnum): ClientEvents()
    data class Connected(val serverId: String): ClientEvents()

    data class PayloadReceived(val payload: Payload): ClientEvents()
}

enum class ConnectionStatusEnum{
    NONE,
    CONNECTING,
    FAILED_TO_CONNECT,
    CONNECTED,
    KICKED,
    LEFT
}


class ClientManager(
    private val connectionsClient: ConnectionsClient,
    private val events: (ClientEvents) -> (Unit)
) {
    fun startDiscovery(context: Context, nickname: String) {
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        connectionsClient.stopDiscovery()

        connectionsClient.startDiscovery(context.packageName, endpointDiscoveryCallback(nickname), discoveryOptions)
            .addOnSuccessListener {
                Log.e("", "CLIENT DISCOVERY READY")
                events(ClientEvents.ConnectionStatus(ConnectionStatusEnum.CONNECTING))
            }
            .addOnFailureListener {
                Log.e("", "CLIENT DISCOVERY FAILURE " + it.message)
                events(ClientEvents.ConnectionStatus(ConnectionStatusEnum.FAILED_TO_CONNECT))
            }
    }

    val endpointDiscoveryCallback: (String) -> EndpointDiscoveryCallback = { nickname ->
        object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                Log.e("", "CLIENT $nickname is requesting connection on: ${info.endpointName} + $endpointId")

                connectionsClient.requestConnection(
                    nickname,
                    endpointId,
                    connectionLifecycleCallback
                ).addOnSuccessListener {
                    Log.e("", "CLIENT Successfully requested a connection")
                }.addOnFailureListener {
                    Log.e("", "CLIENT Failed to request the connection")
                    events(ClientEvents.ConnectionStatus(ConnectionStatusEnum.FAILED_TO_CONNECT))
                }
            }

            override fun onEndpointLost(endpointId: String) {
                events(ClientEvents.ConnectionStatus(ConnectionStatusEnum.KICKED))
                Log.e("", "CLIENT onEndpointLost " + endpointId)
            }
        }
    }

    var connectionLifecycleCallback: ConnectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            when (resolution.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    events(ClientEvents.Connected(endpointId))
                    Log.e("", "CLIENT ConnectionsStatusCodes.STATUS_OK " + endpointId)
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.e("", "CLIENT ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED " + endpointId)
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    Log.e("", "CLIENT ConnectionsStatusCodes.STATUS_ERROR " + endpointId)
                }
                else -> {
                    Log.e("", "CLIENT Unknown status code ${resolution.status.statusCode}")
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            events(ClientEvents.ConnectionStatus(ConnectionStatusEnum.KICKED))
            Log.e("", "CLIENT $endpointId disconnected")
        }
    }

    val payloadCallback: PayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            events(ClientEvents.PayloadReceived(payload))
            Log.e("", "CLIENT onPayloadReceived")
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            Log.e("", "CLIENT onPayloadTransferUpdate")
        }
    }
}