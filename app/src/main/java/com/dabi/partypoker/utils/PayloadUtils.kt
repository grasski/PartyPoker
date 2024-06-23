package com.dabi.partypoker.utils

import android.util.Log
import com.google.android.gms.nearby.connection.Payload
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import javax.inject.Inject


enum class ServerPayloadType{
    CLIENT_CONNECTED,
    ROOM_IS_FULL,

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
    val gson = GsonBuilder()
        .registerTypeAdapter(UiTexts::class.java, UiTextsAdapter())
        .create()

    val json = gson.toJson(Pair(payloadType, data))
    return Payload.fromBytes(json.toByteArray(Charsets.UTF_8))
}
fun <T> fromServerPayload(payload: Payload): Pair<ServerPayloadType, T>{
    val gson = GsonBuilder()
        .registerTypeAdapter(UiTexts::class.java, UiTextsAdapter())
        .create()

    val rawData = String(payload.asBytes()!!, Charsets.UTF_8)
    return gson.fromJson(rawData, object : TypeToken<Pair<ServerPayloadType, T>>(){}.type)
}

fun <T> toClientPayload(payloadType: ClientPayloadType, data: T?): Payload {
    val gson = GsonBuilder()
        .registerTypeAdapter(UiTexts::class.java, UiTextsAdapter())
        .create()

    val json = gson.toJson(Pair(payloadType, data))
    return Payload.fromBytes(json.toByteArray(Charsets.UTF_8))
}
fun <T> fromClientPayload(payload: Payload): Pair<ClientPayloadType, T?>{
    val gson = GsonBuilder()
        .registerTypeAdapter(UiTexts::class.java, UiTextsAdapter())
        .create()

    val rawData = String(payload.asBytes()!!, Charsets.UTF_8)
    return gson.fromJson(rawData, object : TypeToken<Pair<ClientPayloadType, T?>>(){}.type)
}
