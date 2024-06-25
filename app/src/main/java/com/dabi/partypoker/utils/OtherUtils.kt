package com.dabi.partypoker.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.annotation.ArrayRes
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.lang.reflect.Type
import kotlinx.serialization.Serializable


@SuppressLint("DefaultLocale")
fun Int.formatNumberToString(): String {
    return when {
        this >= 1000000 -> String.format("%.1fM", this / 1000000.0).replace(".0", "") + " $"
        this >= 1000 -> String.format("%.1fK", this / 1000.0).replace(".0", "") + " $"
        else -> this.toString() + " $"
    }
}

@Serializable
sealed class UiTexts{
    class StringResource(
        @StringRes val resId: Int,
        vararg val args: Any
    ): UiTexts()

    class PluralResource(
        @PluralsRes val resId: Int,
        val quantity: Int,
        vararg val args: Any
    ): UiTexts()

    class ArrayResource(
        @ArrayRes val resId: Int,
        val index: Int
    ): UiTexts()


    @Composable
    fun asString(): String{
        return when(this){
            is StringResource -> stringResource(resId, *args)
            is PluralResource -> pluralStringResource(resId, quantity, *args)
            is ArrayResource -> stringArrayResource(resId)[index]
        }
    }
    fun asString(context: Context): String{
        return when(this){
            is StringResource -> context.getString(resId, *args)
            is PluralResource -> context.resources.getQuantityString(resId, quantity, *args)
            is ArrayResource -> context.resources.getStringArray(resId)[index]
        }
    }



    fun asArray(): List<String> {
        return when(this){
            is ArrayResource -> Resources.getSystem().getStringArray(resId).toList()
            else -> {
                listOf<String>()}
        }
    }
}



class UiTextsAdapter : JsonSerializer<UiTexts>, JsonDeserializer<UiTexts> {
    override fun serialize(src: UiTexts, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject()
        when (src) {
            is UiTexts.StringResource -> {
                jsonObject.addProperty("type", "StringResource")
                jsonObject.addProperty("resId", src.resId)
                jsonObject.add("args", context.serialize(src.args))
            }
            is UiTexts.PluralResource -> {
                jsonObject.addProperty("type", "PluralResource")
                jsonObject.addProperty("resId", src.resId)
                jsonObject.addProperty("quantity", src.quantity)
                jsonObject.add("args", context.serialize(src.args))
            }
            is UiTexts.ArrayResource -> {
                jsonObject.addProperty("type", "ArrayResource")
                jsonObject.addProperty("resId", src.resId)
                jsonObject.addProperty("index", src.index)
            }
        }
        return jsonObject
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): UiTexts {
        val jsonObject = json.asJsonObject
        val type = jsonObject.get("type").asString
        return when (type) {
            "StringResource" -> UiTexts.StringResource(
                jsonObject.get("resId").asInt,
                *context.deserialize(jsonObject.get("args"), Array<Any>::class.java)
            )
            "PluralResource" -> UiTexts.PluralResource(
                jsonObject.get("resId").asInt,
                jsonObject.get("quantity").asInt,
                *context.deserialize(jsonObject.get("args"), Array<Any>::class.java)
            )
            "ArrayResource" -> UiTexts.ArrayResource(
                jsonObject.get("resId").asInt,
                jsonObject.get("index").asInt
            )
            else -> throw JsonParseException("Unknown type: $type")
        }
    }
}