package com.dabi.partypoker.featureClient.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.dabi.partypoker.R


@Composable
fun PlayerViewPrivate(
//    navController: NavController,
//    playerState: PlayerState,
//    playerActionsState: PlayerActionsState,
//    onPlayerEvent: (PlayerEvents) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ){
        Column(
            modifier = Modifier
                .fillMaxHeight(0.7f)
                .fillMaxWidth()

                .drawWithContent {
                    drawContent()

                    fun addPath(
                        heightOffset: Dp,
                    ): Path {
                        val path = Path()
                        path.arcTo(
                            rect = Rect(
                                offset = Offset(
                                    -10.dp.toPx(),
                                    size.height - 80.dp.toPx() - heightOffset.toPx()
                                ),
                                size = Size(80.dp.toPx(), 80.dp.toPx())
                            ),
                            startAngleDegrees = 90f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = true,
                        )
                        path.moveTo(30.dp.toPx(), size.height - heightOffset.toPx())
                        path.lineTo(size.width - 30.dp.toPx(), size.height - heightOffset.toPx())
                        path.arcTo(
                            rect = Rect(
                                offset = Offset(
                                    size.width - 70.dp.toPx(),
                                    size.height - 80.dp.toPx() - heightOffset.toPx()
                                ),
                                size = Size(80.dp.toPx(), 80.dp.toPx())
                            ),
                            startAngleDegrees = 0f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = true,
                        )

                        return path
                    }

                    val path1 = addPath(0.dp)
                    val path2 = addPath(6.dp)
                    val path3 = addPath((-6).dp)


                    drawPath(
                        path = path1,
                        color = Color.DarkGray,
                        style = Stroke(12.dp.toPx())
                    )
                    drawPath(
                        path = path2,
                        color = Color.White.copy(alpha = 0.5f),
                        style = Stroke(1.dp.toPx())
                    )
                    drawPath(
                        path = path3,
                        color = Color.White,
                        style = Stroke(1.dp.toPx())
                    )
                }
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .paint(
                    rememberAsyncImagePainter(model = R.drawable.table),
                    contentScale = ContentScale.Crop
                )
                .padding(11.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            Image(
                painterResource(id = R.drawable.player_1),
                null,
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color.Black.copy(alpha = 0.5f), CircleShape)
            )
        }
    }
}