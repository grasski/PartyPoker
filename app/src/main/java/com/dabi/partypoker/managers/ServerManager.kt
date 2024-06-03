package com.dabi.partypoker.managers

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy


sealed class ServerEvents{
    data class StartServer(val context: Context, val serverType: ServerType): ServerEvents()
    data class ServerStatus(val status: ServerStatusEnum): ServerEvents()

    data class ClientConnected(val endpointId: String): ServerEvents()
    data class ClientDisconnected(val endpointId: String): ServerEvents()

    data class PayloadReceived(val receivedFrom: String, val payload: Payload): ServerEvents()
}

enum class ServerStatusEnum{
    OFF,
    ADVERTISING,
    ADVERTISING_FAILED,
    ACTIVE // After connection is established with all players, we can turn off the advertising
}

enum class ServerType{
    IS_PLAYER,
    IS_TABLE
}


class ServerManager(
    private val connectionsClient: ConnectionsClient,
    private val events: (ServerEvents) -> (Unit)
) {
    fun closeServer(){
        connectionsClient.stopAdvertising()
        connectionsClient.stopDiscovery()
        connectionsClient.stopAllEndpoints()
    }

    fun startAdvertising(context: Context, strategy: Strategy = Strategy.P2P_STAR, name: String = "Server Owner") {
        val advertisingOptions: AdvertisingOptions = AdvertisingOptions.Builder()
            .setStrategy(strategy).build()
        connectionsClient.stopAdvertising()
        connectionsClient.stopAllEndpoints()

        connectionsClient.startAdvertising(name, context.packageName, connectionLifecycleCallback, advertisingOptions)
            .addOnSuccessListener {
                events(ServerEvents.ServerStatus(ServerStatusEnum.ADVERTISING))
                Log.e("", "SERVER ADVERTISING READY")
            }
            .addOnFailureListener {
                events(ServerEvents.ServerStatus(ServerStatusEnum.ADVERTISING_FAILED))
                Log.e("", "SERVER ADVERTISING FAILURE")
            }
    }


    private val connectionLifecycleCallback: ConnectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
            when (resolution.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    events(ServerEvents.ClientConnected(endpointId))
                    Log.e("", "SERVER ConnectionsStatusCodes.STATUS_OK " + endpointId)
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.e("", "SERVER ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED " + endpointId)
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    Log.e("", "SERVER ConnectionsStatusCodes.STATUS_ERROR " + endpointId)
                }
                else -> {
                    Log.e("", "SERVER Unknown status code ${resolution.status.statusCode}")
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            events(ServerEvents.ClientDisconnected(endpointId))
            Log.e("", "SERVER $endpointId disconnected")
        }
    }


    private val payloadCallback: PayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            events(ServerEvents.PayloadReceived(endpointId, payload))
            Log.e("", "SERVER onPayloadReceived")
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            Log.e("", "SERVER onPayloadTransferUpdate")
        }
    }
}