package com.dabi.partypoker.featureCore.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
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
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.window.core.layout.WindowHeightSizeClass
import coil.compose.rememberAsyncImagePainter
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.dabi.partypoker.R
import com.dabi.partypoker.featureClient.model.data.PlayerState
import com.dabi.partypoker.featureCore.data.PlayerLayoutDirection
import com.dabi.partypoker.featureCore.data.colors
import com.dabi.partypoker.featureServer.model.data.GameState
import com.dabi.partypoker.managers.GameEvents
import com.dabi.partypoker.utils.CardsUtils
import com.dabi.partypoker.utils.formatNumberToString


@Composable
fun LoadingAnimation(
    modifier: Modifier,
    text: String,
    onCancelRequest: () -> Unit
) {
    val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.loading_animation))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.weight(1f)
        )
        Text(text = text)
        Button(onClick = { onCancelRequest() }) {
            Text(text = "Cancel")
        }
    }
}


@Composable
fun GamePopUpMenu(
    navController: NavController,
    isPlayer: Boolean,
    onDismissRequest: () -> Unit,
    onLeaveRequest: () -> Unit,
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
        ){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when(isPlayer){
                    true -> {
                        //TODO: in future some settings, etc.
                        Button(onClick = {
                            onDismissRequest()
                            onLeaveRequest()
                        }) {
                            Text(text = "Leave")
                        }
                    }
                    false -> {
                        //TODO: in future some settings, kicking people, etc.
                        Button(onClick = { /*TODO*/ }) {
                            Text(text = "Stop advertising")
                        }
                        Button(onClick = {
                            onDismissRequest()
                            onLeaveRequest()
                        }) {
                            Text(text = "Leave")
                        }
                    }
                }

                Row (
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.End
                ){
                    Button(onClick = { onDismissRequest() }) {
                        Text(text = "Pokračovat")
                    }
                }
            }
        }
    }
}


@Composable
fun GameTable(
    modifier: Modifier = Modifier,
    gameState: GameState,

    isServer: Boolean,
    onGameEvent: (GameEvents) -> Unit,

    tableInfo: (Offset, IntSize) -> Unit
) {
    var tablePosition by remember { mutableStateOf(Offset.Zero) }
    var tableSize by remember { mutableStateOf(IntSize.Zero) }

    var playerBoxSize by remember { mutableStateOf(DpSize.Zero) }
    var fontSize by remember { mutableStateOf(20.sp) }
    CalculatePlayerBoxSize(
        playerBoxSize = { playerBoxSize = it },
        fontSize = { fontSize = it }
    )

    Column(
        modifier = modifier
            .onGloballyPositioned {
                tablePosition = it.positionInRoot()
                tableSize = it.size

                tableInfo(tablePosition, tableSize)
            }
            .clip(RoundedCornerShape(40.dp))
            .border(1.dp, Color.White, RoundedCornerShape(40.dp))
            .border(10.dp, Color.DarkGray, RoundedCornerShape(40.dp))
            .border(11.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(40.dp))
            .paint(
                rememberAsyncImagePainter(model = R.drawable.table),
                contentScale = ContentScale.FillBounds
            )
            .padding(11.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
//        if (gameState.started){
            Row(
                modifier = Modifier
                    .fillMaxWidth(.60f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (i in 0..4){
                    val card = gameState.cardsTable.getOrNull(i)
                    card?.let {
                        val cardId = CardsUtils.cardIDs[card.type.name.lowercase() + "_" + card.value]
                        CardBox(cardId, Modifier.weight(1f))
                    } ?: run {
                        CardBox(null, Modifier.weight(1f))
                    }
                }
            }

            Row (
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(playerBoxSize.height / 2)
                    .padding(horizontal = 5.dp),

                ){
                Row(
                    modifier = Modifier
                        .width(playerBoxSize.width),
                    horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.bank),
                        contentDescription = "bank",
                    )
                    Text(
                        text = gameState.bank.formatNumberToString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = fontSize,
                        maxLines = 1,
                        color = Color.White,
                        modifier = Modifier
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .padding(5.dp),
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(0.35f)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 5.dp)
                    ){
                        Text(
                            text = gameState.messages.lastOrNull()?.asString() ?: "",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = fontSize,
                            color = Color.White,
                        )
                    }

                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "",
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = {
                                //TODO()
                            }),
                        tint = Color.White
                    )
                }
            }
//        } else{
//            if (isServer){
//                Button(onClick = {
//                    onGameEvent(GameEvents.StartGame)
//                }) {
//                    Text(text = "Start the game")
//                }
//            }
//        }
    }
}


@Composable
fun CardBox(cardId: Int?, modifier: Modifier) {
    cardId?.let { id ->
        Image(
            painter = painterResource(id = id),
            contentDescription = "card",
            modifier = modifier
                .padding(5.dp)
                .border(1.dp, Color.White.copy(alpha = 1f), RoundedCornerShape(6.dp))
                .padding(5.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    } ?: run {
        Image(
            painter = painterResource(id = R.drawable.clubs_2),
            contentDescription = "card",
            modifier = modifier
                .padding(5.dp)
                .border(1.dp, Color.White.copy(alpha = 1f), RoundedCornerShape(6.dp))
                .padding(5.dp)
                .alpha(0f)
        )
    }
}
