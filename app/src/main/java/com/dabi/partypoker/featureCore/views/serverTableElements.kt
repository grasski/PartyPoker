package com.dabi.partypoker.featureCore.views

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dabi.partypoker.R
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureCore.data.PlayerLayoutDirection
import com.dabi.partypoker.featureCore.data.colors
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.managers.ServerType
import com.dabi.partypoker.utils.CardsUtils
import com.dabi.partypoker.utils.formatNumberToString


@Composable
fun DrawPlayersByPosition(
    players: Map<Int, PlayerState?>,
    myPosition: Int = 0,
    serverType: ServerType,
    tablePosition: Offset,
    tableSize: IntSize,
) {
    var playerBoxSize by remember { mutableStateOf(DpSize.Zero) }
    var fontSize by remember { mutableStateOf(20.sp) }
    CalculatePlayerBoxSize(
        playerBoxSize = { playerBoxSize = it },
        fontSize = { fontSize = it }
    )

    val density = LocalDensity.current
    val topLeftHorizontal = with(density) { DpOffset(
        x = tablePosition.x.toDp() - playerBoxSize.width / 2,
        y = tablePosition.y.toDp() - playerBoxSize.height / 2 - playerBoxSize.height / 3
    ) }
    val bottomLeftHorizontal = with(density) { DpOffset(
        x = tablePosition.x.toDp() - playerBoxSize.width / 2,
        y = tablePosition.y.toDp() + tableSize.height.toDp() - playerBoxSize.height / 2 + playerBoxSize.height / 3
    ) }


    val topLeftVertical = with(density) { DpOffset(
        x = tablePosition.x.toDp() - playerBoxSize.width / 2 - playerBoxSize.width / 5,
        y = tablePosition.y.toDp() + playerBoxSize.height / 2
    ) }
    val topRightVertical = with(density) { DpOffset(
        x = tablePosition.x.toDp() + tableSize.width.toDp() - playerBoxSize.width / 2 + playerBoxSize.width / 5,
        y = tablePosition.y.toDp() + playerBoxSize.height / 2
    ) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val totalPlayersHorizontal = 4
        val totalPlayersVertical = if (serverType == ServerType.IS_TABLE) 1 else 2

        val originalIndex = if (serverType == ServerType.IS_TABLE) 0 else (myPosition + 2)
        var seatIndex = originalIndex

        // Left side
        Column(
            modifier = Modifier
                .offset(topLeftVertical.x, topLeftVertical.y)
                .size(
                    width = playerBoxSize.width,
                    height = with(density) { tableSize.height.toDp() - playerBoxSize.height }
                ),
            verticalArrangement = Arrangement.SpaceEvenly
        ){
            for (i in (totalPlayersVertical - 1 + seatIndex) downTo (0 + originalIndex)){
                PlayerBox(size = playerBoxSize, fontSize = fontSize, playerState = players[i % 10], layoutDirection = PlayerLayoutDirection.LEFT)

                seatIndex ++
                if (seatIndex >= 10){
                    seatIndex = 0
                }
            }
        }

        // Top side
        Row(
            modifier = Modifier
                .offset(topLeftHorizontal.x, topLeftHorizontal.y)
                .size(
                    width = with(density) { tableSize.width.toDp() + playerBoxSize.width },
                    height = playerBoxSize.height
                ),
            horizontalArrangement = Arrangement.spacedBy(playerBoxSize.width/4, Alignment.CenterHorizontally)
        ){
            for (i in seatIndex..<(totalPlayersHorizontal + totalPlayersVertical + originalIndex)){
                PlayerBox(size = playerBoxSize, fontSize = fontSize, playerState = players[i % 10], layoutDirection = PlayerLayoutDirection.TOP)

                seatIndex ++
                if (seatIndex >= 10){
                    seatIndex = 0
                }
            }
        }

        // Right side
        Column(
            modifier = Modifier
                .offset(topRightVertical.x, topRightVertical.y)
                .size(
                    width = playerBoxSize.width,
                    height = with(density) { tableSize.height.toDp() - playerBoxSize.height }
                ),
            verticalArrangement = Arrangement.SpaceEvenly
        ){
            if (serverType == ServerType.IS_TABLE){
                for (i in seatIndex..<(2*totalPlayersVertical + totalPlayersHorizontal)){
                    PlayerBox(size = playerBoxSize, fontSize = fontSize, playerState = players[i], layoutDirection = PlayerLayoutDirection.RIGHT)
                    seatIndex ++
                }
            } else{
                for (i in (seatIndex)..<(seatIndex + totalPlayersVertical)){
                    PlayerBox(size = playerBoxSize, fontSize = fontSize, playerState = players[i % 10], layoutDirection = PlayerLayoutDirection.RIGHT)

                    seatIndex ++
                    if (seatIndex >= 10){
                        seatIndex = 0
                    }
                }
            }
        }

        // Bottom side
        Row(
            modifier = Modifier
                .offset(bottomLeftHorizontal.x, bottomLeftHorizontal.y)
                .size(
                    width = with(density) { tableSize.width.toDp() + playerBoxSize.width },
                    height = playerBoxSize.height
                ),
            horizontalArrangement =
            if (serverType == ServerType.IS_TABLE)
                Arrangement.spacedBy(playerBoxSize.width/4, Alignment.CenterHorizontally)
            else
                Arrangement.Start
        ){
            if (serverType == ServerType.IS_TABLE){
                for (i in (2*totalPlayersHorizontal + 2*totalPlayersVertical -1) downTo seatIndex ){
                    PlayerBox(size = playerBoxSize, fontSize = fontSize, playerState = players[i], layoutDirection = PlayerLayoutDirection.BOTTOM)
                    seatIndex ++
                }
            } else{
                PlayerBox(size = playerBoxSize, fontSize = fontSize, playerState = players[(seatIndex+1) % 10], layoutDirection = PlayerLayoutDirection.BOTTOM)
            }
        }
    }
}
