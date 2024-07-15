package com.dabi.partypoker.featureCore.data

import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


enum class PlayerLayoutDirection{
    LEFT,
    TOP,
    RIGHT,
    BOTTOM
}


object colors{
    val calledMoneyColor = Color(0x99ada880).copy(alpha = 1f)

    val playerButtonsColor = Color(0x992b332f).copy(alpha = 1f)
    val playerButtonsColor2 = Color(0x99332b2b).copy(alpha = 1f)

    val buttonColor = Color(0x99364536).copy(alpha = 1f)

    val playerBoxColor1 = Color(0x9920382c).copy(alpha = 1f)
    val playerBoxColor2 = Color(0x997da892).copy(alpha = 1f)

    val messagesCard = Color(0x994b7a80).copy(alpha = 1f)

    val menuButton = Color(0x9933b249).copy(alpha = 1f)
}


@Composable
fun TextFieldDefaults.myColors() = this.colors(
    unfocusedContainerColor = colors.buttonColor,
    focusedContainerColor = colors.buttonColor,
    focusedTextColor = colors.calledMoneyColor,
    unfocusedTextColor = colors.calledMoneyColor,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    cursorColor = colors.calledMoneyColor,
    selectionColors = TextSelectionColors(
        handleColor = colors.calledMoneyColor,
        backgroundColor = colors.calledMoneyColor.copy(alpha = 0.2f)
    ),
    unfocusedPlaceholderColor = colors.calledMoneyColor.copy(alpha = 0.8f),
    focusedPlaceholderColor = colors.calledMoneyColor.copy(alpha = 0.8f),

    unfocusedLabelColor = colors.calledMoneyColor.copy(alpha = 0.8f),
    focusedLabelColor = colors.calledMoneyColor.copy(alpha = 0.8f),

    focusedTrailingIconColor = colors.calledMoneyColor,
    unfocusedTrailingIconColor = colors.calledMoneyColor,
)