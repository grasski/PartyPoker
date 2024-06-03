package com.dabi.partypoker.utils

import com.google.android.gms.nearby.connection.Payload
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


enum class ServerPayloadType{
    CLIENT_CONNECTED,

    UPDATE_CLIENT,
    UPDATE_GAME_STATE,
}
enum class ClientPayloadType{
    CONNECTED,
    ACTION_DISCONNECTED,
    ACTION_READY,

    ACTION_CHECK,
    ACTION_CALL,
    ACTION_RAISE,
    ACTION_FOLD
}


fun <T> toServerPayload(payloadType: ServerPayloadType, data: T): Payload {
    val json = Gson().toJson(Pair(payloadType, data))
    return Payload.fromBytes(json.toByteArray(Charsets.UTF_8))
}
fun <T> fromServerPayload(payload: Payload): Pair<ServerPayloadType, T>{
    val rawData = String(payload.asBytes()!!, Charsets.UTF_8)
    return Gson().fromJson(rawData, object : TypeToken<Pair<ServerPayloadType, T>>(){}.type)
}

fun <T> toClientPayload(payloadType: ClientPayloadType, data: T?): Payload {
    val json = Gson().toJson(Pair(payloadType, data))
    return Payload.fromBytes(json.toByteArray(Charsets.UTF_8))
}
fun <T> fromClientPayload(payload: Payload): Pair<ClientPayloadType, T?>{
    val rawData = String(payload.asBytes()!!, Charsets.UTF_8)
    return Gson().fromJson(rawData, object : TypeToken<Pair<ClientPayloadType, T?>>(){}.type)
}
