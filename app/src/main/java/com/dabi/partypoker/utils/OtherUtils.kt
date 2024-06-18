package com.dabi.partypoker.utils

import android.annotation.SuppressLint
import android.content.res.Resources
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource


@SuppressLint("DefaultLocale")
fun Int.formatNumberToString(): String {
    return when {
        this >= 1000000 -> String.format("%.1fM", this / 1000000.0).replace(".0", "")
        this >= 1000 -> String.format("%.1fK", this / 1000.0).replace(".0", "")
        else -> this.toString()
    }
}


sealed class UiTexts{
    class StringResource(
        @StringRes val resId: Int,
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
            is ArrayResource -> stringArrayResource(resId)[index]
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

