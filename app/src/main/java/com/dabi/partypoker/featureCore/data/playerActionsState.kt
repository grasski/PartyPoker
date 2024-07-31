package com.dabi.partypoker.featureCore.data


data class PlayerActionsState(
    val canCheck: Boolean = true,
    val callAmount: Int = 0,
    val raiseAmount: Int = 0,
    val canFold: Boolean = true
)
