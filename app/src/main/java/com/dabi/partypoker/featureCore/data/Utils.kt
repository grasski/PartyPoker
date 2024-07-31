package com.dabi.partypoker.featureCore.data

import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.dabi.partypoker.ui.theme.textColor


enum class PlayerLayoutDirection{
    LEFT,
    TOP,
    RIGHT,
    BOTTOM
}


@Composable
fun TextFieldDefaults.myColors() = this.colors(
    unfocusedContainerColor = MaterialTheme.colorScheme.primary,
    focusedContainerColor = MaterialTheme.colorScheme.primary,
    focusedTextColor = textColor,
    unfocusedTextColor = textColor,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    cursorColor = textColor,
    selectionColors = TextSelectionColors(
        handleColor = textColor,
        backgroundColor = textColor.copy(alpha = 0.2f)
    ),
    unfocusedPlaceholderColor = textColor.copy(alpha = 0.8f),
    focusedPlaceholderColor = textColor.copy(alpha = 0.8f),

    unfocusedLabelColor = textColor.copy(alpha = 0.8f),
    focusedLabelColor = textColor.copy(alpha = 0.8f),

    focusedTrailingIconColor = textColor,
    unfocusedTrailingIconColor = textColor,
)


@Composable
fun TextFieldDefaults.myColorsSettings() = this.colors(
    unfocusedContainerColor = MaterialTheme.colorScheme.onSecondaryContainer,
    focusedContainerColor = MaterialTheme.colorScheme.onSecondaryContainer,
    focusedTextColor = MaterialTheme.colorScheme.onSecondary,
    unfocusedTextColor = MaterialTheme.colorScheme.onSecondary,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    cursorColor = MaterialTheme.colorScheme.onSecondary,
    selectionColors = TextSelectionColors(
        handleColor = MaterialTheme.colorScheme.onSecondary,
        backgroundColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.2f)
    ),
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f),
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f),

    unfocusedLabelColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f),
    focusedLabelColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f),

    focusedTrailingIconColor = MaterialTheme.colorScheme.onSecondary,
    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSecondary,
)