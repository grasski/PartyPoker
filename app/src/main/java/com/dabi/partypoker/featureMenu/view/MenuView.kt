package com.dabi.partypoker.featureMenu.view

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RawRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Start
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.util.fastRoundToInt
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.model.animatable.AnimatableValue
import com.dabi.partypoker.MenuScreen
import com.dabi.partypoker.PlayerScreen
import com.dabi.partypoker.R
import com.dabi.partypoker.ServerScreen
import com.dabi.partypoker.featureClient.viewmodel.PlayerViewModel
import com.dabi.partypoker.featureCore.data.colors
import com.dabi.partypoker.featureCore.views.AutoSizeText
import com.dabi.partypoker.featureMenu.viewModel.MenuGameSettingsEvent
import com.dabi.partypoker.featureMenu.viewModel.MenuViewModel
import com.dabi.partypoker.featureServer.viewmodel.ServerOwnerViewModel
import com.dabi.partypoker.managers.ClientEvents
import com.dabi.partypoker.managers.ServerEvents
import com.dabi.partypoker.managers.ServerType
import com.dabi.partypoker.repository.gameSettings.GameSettings
import com.dabi.partypoker.utils.UiTexts
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlin.math.max
import kotlin.math.roundToInt


enum class ViewPosition{
    MENU,
    LOCAL_SERVER,
    LOCAL_PLAYER
}


@OptIn(ExperimentalPermissionsApi::class, ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun MenuView(
    navController: NavController
) {
    val context = LocalContext.current
    var viewPosition by rememberSaveable { mutableStateOf(ViewPosition.MENU) }
    var nickname by rememberSaveable { mutableStateOf("") }

    var permissions: MultiplePermissionsState = rememberMultiplePermissionsState(emptyList())
    CheckPermissions(permissionsState = { permissions = it })
    BackHandler(true) {  }
    val density = LocalDensity.current

    Crossfade(
        targetState = viewPosition,
        modifier = Modifier
            .paint(
                painterResource(id = R.drawable.game_start_background),
                contentScale = ContentScale.Crop
            )
            .fillMaxSize(),
    ) {position ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ){
            if (viewPosition != ViewPosition.MENU){
                Button(
                    onClick = {
                        viewPosition = ViewPosition.MENU
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = colors.calledMoneyColor
                    ),
                    contentPadding = PaddingValues(5.dp, vertical = 0.dp),
                    modifier = Modifier
                        .padding(16.dp)
                        .border(2.dp, colors.calledMoneyColor, RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.playerButtonsColor2.copy(0.8f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "back"
                        )
                        Text("Menu")
                    }
                }
            }

            when (position) {
                ViewPosition.MENU -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = {
                                viewPosition = ViewPosition.LOCAL_SERVER
                            },
                            modifier = Modifier
                                .padding(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.buttonColor,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ){
                            AutoSizeText(
                                text = UiTexts.StringResource(R.string.create_local).asString(),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        Button(
                            onClick = {
                                viewPosition = ViewPosition.LOCAL_PLAYER
                            },
                            modifier = Modifier
                                .padding(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.buttonColor,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ){
                            AutoSizeText(
                                text = UiTexts.StringResource(R.string.connect_local).asString(),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
                ViewPosition.LOCAL_SERVER -> {
                    BackHandler(true) {
                        viewPosition = ViewPosition.MENU
                    }

                    if (permissions.allPermissionsGranted) {
                        var localServerPlaying by rememberSaveable { mutableStateOf(false) }
                        var showSettings by rememberSaveable { mutableStateOf(false) }

                        val menuViewModel: MenuViewModel = hiltViewModel()
                        val gameSettings by menuViewModel.gameSettings.collectAsStateWithLifecycle()
                        val selectedSetting by menuViewModel.selectedSetting.collectAsStateWithLifecycle()

                        BoxWithConstraints(
                            modifier = Modifier.fillMaxSize()
//                                .pointerInput(Unit) {
//                                    detectTapGestures {
//                                        Log.e("", "OFFSET: " + it)
//                                        // Kliknutí bylo zachyceno BoxWithConstraints
//                                        if (showSettings && it.x < this.size.width/2) {
//                                            showSettings = false
//                                        }
//                                    }
//                                }
//                                .clickable(
//                                    interactionSource = null,
//                                    indication = null,
//                                    onClick = {
//                                        if (showSettings){
//                                            showSettings = false
//                                        }
//                                    }
//                                )
                        ) {
                            val maxHeight = this.constraints.maxHeight
                            var textFieldSize by remember { mutableStateOf(DpSize.Zero) }
                            var heightOfCenter by remember { mutableStateOf(IntSize.Zero) }
                            var selectedAvatar by rememberSaveable { mutableIntStateOf(0) }

                            val animateOffset = remember {
                                Animatable(
                                    initialValue = if (localServerPlaying)
                                        (maxHeight / 2).minus(heightOfCenter.height).toFloat().minus(with(density){ 20.dp.toPx() })
                                    else
                                        (maxHeight / 2).minus(heightOfCenter.height.div(2)).toFloat()
                                )
                            }
                            LaunchedEffect(localServerPlaying) {
                                animateOffset.animateTo(
                                    if (localServerPlaying)
                                        (maxHeight / 2).minus(heightOfCenter.height).toFloat().minus(with(density){ 20.dp.toPx() })
                                    else
                                        (maxHeight / 2).minus(heightOfCenter.height.div(2)).toFloat()
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset {
                                        IntOffset(
                                            x = 0,
                                            y = animateOffset.value.roundToInt()
                                        )
                                    }
                                    .onGloballyPositioned {
                                        heightOfCenter = it.size
                                    },
                                verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.Bottom),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(
                                            2.dp,
                                            colors.calledMoneyColor,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .background(colors.playerButtonsColor2.copy(0.8f))
                                        .widthIn(220.dp)
                                        .onGloballyPositioned {
                                            textFieldSize = with(density) {
                                                DpSize(
                                                    it.size.width.toDp(),
                                                    it.size.height.toDp()
                                                )
                                            }
                                        }
                                        .padding(start = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ){
                                    AutoSizeText(
                                        text = UiTexts.StringResource(R.string.server_playing).asString(),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = Color.White
                                        )
                                    )
                                    Checkbox(
                                        checked = localServerPlaying,
                                        onCheckedChange = { localServerPlaying = it },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = colors.calledMoneyColor,
                                            uncheckedColor = Color.White
                                        )
                                    )
                                }

                                Crossfade(
                                    targetState = localServerPlaying,
                                ) { isPlaying ->
                                    Row(
                                        modifier = Modifier
                                            .padding(start = 55.dp)
                                    ) {
                                        if (isPlaying) {
                                            TextField(
                                                value = nickname,
                                                onValueChange = { nickname = it },
                                                placeholder = {
                                                    AutoSizeText(
                                                        text = UiTexts.StringResource(R.string.enter_nickname).asString(),
                                                        style = TextStyle(
                                                            fontWeight = FontWeight.ExtraBold,
                                                            fontSize = 20.sp,
                                                        )
                                                    )
                                                },
                                                textStyle = TextStyle(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 20.sp,
                                                    platformStyle = null
                                                ),
                                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                                colors = TextFieldDefaults.colors(
                                                    unfocusedContainerColor = colors.buttonColor,
                                                    focusedContainerColor = colors.playerButtonsColor2,
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
                                                    focusedPlaceholderColor = colors.calledMoneyColor.copy(alpha = 0.8f)
                                                ),
                                                trailingIcon = {
                                                    IconButton(
                                                        onClick = {
                                                            if (nickname.isNotBlank()){
                                                                navController.navigate(
                                                                    ServerScreen(
                                                                        serverType = ServerType.IS_PLAYER.toString(),
                                                                        serverName = nickname.ifEmpty { "ServerOwner" },
                                                                        avatarId = selectedAvatar
                                                                    )
                                                                ) {
                                                                    popUpTo(MenuScreen){inclusive = true}
                                                                }
                                                            }
                                                        },
                                                        enabled = nickname.isNotBlank(),
                                                        colors = IconButtonDefaults.iconButtonColors(
                                                            containerColor = Color.Transparent,
                                                            contentColor = colors.calledMoneyColor
                                                        )
                                                    ){
                                                        Icon(
                                                            Icons.Default.Start,
                                                            ""
                                                        )
                                                    }
                                                },
                                                singleLine = true,
                                                modifier = Modifier
                                                    .width(textFieldSize.width)
                                                    .height(55.dp),
                                                shape = RoundedCornerShape(10.dp),
                                            )
                                        }
                                        else {
                                            Box(
                                                modifier = Modifier
                                                    .width(textFieldSize.width)
                                                    .height(55.dp),
                                                contentAlignment = Alignment.Center
                                            ){
                                                Button(
                                                    onClick = {
                                                        navController.navigate(
                                                            ServerScreen(
                                                                serverType = if(localServerPlaying) ServerType.IS_PLAYER.toString() else ServerType.IS_TABLE.toString(),
                                                                serverName = nickname.ifEmpty { "ServerOwner" },
                                                                avatarId = 0
                                                            )
                                                        ) {
                                                            popUpTo(MenuScreen){inclusive = true}
                                                        }
                                                    },
                                                    modifier = Modifier
                                                        .fillMaxSize(),
                                                    contentPadding = PaddingValues(horizontal = 5.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = colors.buttonColor,
                                                        contentColor = Color.White
                                                    ),
                                                    shape = RoundedCornerShape(10.dp)
                                                ){
                                                    AutoSizeText(
                                                        text = UiTexts.StringResource(R.string.server_start).asString(),
                                                        style = MaterialTheme.typography.titleLarge
                                                    )
                                                }
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .width(55.dp)
                                                .height(55.dp)
                                                .padding(5.dp),
                                        ){
                                            FloatingActionButton(
                                                onClick = {
                                                    showSettings = true
                                                },
                                                containerColor = colors.playerButtonsColor2,
                                                contentColor = Color.White
                                            ) {
                                                Icon(
                                                    Icons.Filled.Settings,
                                                    "settings",
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(5.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxHeight(0.5f)
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                            ){
                                AnimatedContent(
                                    targetState = localServerPlaying,
                                    transitionSpec = {
                                        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                                    }
                                ) {isPlaying ->
                                    if (isPlaying){
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize(),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                        ){
                                            AvatarsSelectionView{ selectedAvatar = it }
                                        }
                                    } else{
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Transparent)
                                        ){}
                                    }
                                }
                            }


                            val orientation = LocalConfiguration.current.orientation
                            AnimatedVisibility(
                                visible = showSettings,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd),
                                enter = if (orientation == Configuration.ORIENTATION_PORTRAIT)  slideInVertically { it } else slideInHorizontally { it },
                                exit = if (orientation == Configuration.ORIENTATION_PORTRAIT) slideOutVertically { it } else slideOutHorizontally { it }
                            ) {
                                val newSettings by remember { mutableStateOf(GameSettings("")) }
                                var newSettingsActive by remember { mutableStateOf(false) }

                                var activeSettings by remember { mutableStateOf(selectedSetting) }

                                LazyColumn(modifier = Modifier
                                    .fillMaxHeight(if (orientation == Configuration.ORIENTATION_PORTRAIT) 0.5f else 1f)
                                    .fillMaxWidth(if (orientation == Configuration.ORIENTATION_PORTRAIT) 1f else 0.5f)
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 10.dp,
                                            topEnd = if (orientation == Configuration.ORIENTATION_PORTRAIT) 10.dp else 0.dp,
                                            bottomEnd = 0.dp,
                                            bottomStart = if (orientation == Configuration.ORIENTATION_PORTRAIT) 0.dp else 10.dp
                                        )
                                    )
                                    .background(colors.playerButtonsColor)
                                    .padding(8.dp)
                                ){
                                    item{
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ){
                                            IconButton(
                                                onClick = {
                                                    showSettings = false
                                                },
                                                modifier = Modifier
                                                    .align(Alignment.CenterStart)
                                                    .border(
                                                        2.dp,
                                                        colors.calledMoneyColor,
                                                        CircleShape
                                                    )
                                            ) {
                                                Icon(
                                                    Icons.Filled.Close,
                                                    "close",
                                                    tint = colors.calledMoneyColor
                                                )
                                            }

                                            AutoSizeText(
                                                text = "Herní nastavení",
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    textDecoration = TextDecoration.Underline,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = colors.calledMoneyColor
                                                )
                                            )

                                            IconButton(
                                                onClick = {
                                                    newSettingsActive = true
                                                },
                                                modifier = Modifier
                                                    .align(Alignment.CenterEnd)
                                                    .border(
                                                        2.dp,
                                                        colors.calledMoneyColor,
                                                        CircleShape
                                                    )
                                            ) {
                                                Icon(
                                                    Icons.Filled.AddCircle,
                                                    "create",
                                                    tint = colors.calledMoneyColor,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
                                    }

                                    item {
                                        var dropdownExpanded by rememberSaveable { mutableStateOf(false) }
                                        gameSettings.map { it.title }

                                        ExposedDropdownMenuBox(
                                            expanded = dropdownExpanded,
                                            onExpandedChange = {
                                                if (gameSettings.size > 1) {
                                                    dropdownExpanded = !dropdownExpanded
                                                }
                                            },
                                            modifier = Modifier
                                                .fillParentMaxWidth()
                                        ){
                                            TextField(
                                                modifier = Modifier
                                                    .menuAnchor()
                                                    .fillParentMaxWidth(0.8f)
                                                    .align(Alignment.Center),
                                                value = selectedSetting.title,
                                                onValueChange = {},
                                                readOnly = true,
                                                trailingIcon = {
                                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                                        expanded = dropdownExpanded
                                                    )
                                                },
                                                textStyle = TextStyle(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 20.sp,
                                                    platformStyle = null
                                                ),
                                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                                colors = TextFieldDefaults.colors(
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
                                                    focusedTrailingIconColor = colors.calledMoneyColor,
                                                    unfocusedTrailingIconColor = colors.calledMoneyColor
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                            )

                                            if (gameSettings.size > 1){
                                                ExposedDropdownMenu(
                                                    expanded = dropdownExpanded,
                                                    onDismissRequest = { dropdownExpanded = false }
                                                ) {
                                                    gameSettings.forEach {
                                                        DropdownMenuItem(
                                                            text = { Text(it.title) },
                                                            onClick = {
                                                                menuViewModel.onEvent(MenuGameSettingsEvent.SelectSettings(it))
                                                                dropdownExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    item{
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ){
                                            Text(text = "1. Title: ")
                                            Text(text = selectedSetting.title)
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ){
                                            Text(text = "2. Player money: ")
                                            Text(text = selectedSetting.playerMoney.toString())
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ){
                                            Text(text = "3. Small blind amount: ")
                                            Text(text = selectedSetting.smallBlindAmount.toString())
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ){
                                            Text(text = "4. Big blind amount: ")
                                            Text(text = selectedSetting.bigBlindAmount.toString())
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ){
                                            Text(text = "5. Game over timeout: ")
                                            Text(text = selectedSetting.nextGameInMillis.toString())
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ){
                                            Text(text = "6. Player move timeout: ")
                                            Text(text = selectedSetting.playerTimerDurationMillis.toString())
                                        }
                                    }
                                }
                            }
                        }
                    } else{
                        NoPermissionsView(
                            forServer = true
                        )
                    }
                }
                ViewPosition.LOCAL_PLAYER -> {
                    BackHandler(true) {
                        viewPosition = ViewPosition.MENU
                    }
                    if (permissions.allPermissionsGranted) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            var selectedAvatar by rememberSaveable { mutableIntStateOf(0) }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.5f)
                                    .padding(20.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                TextField(
                                    value = nickname,
                                    onValueChange = { nickname = it },
                                    placeholder = { Text(UiTexts.StringResource(R.string.enter_nickname).asString(), fontWeight = FontWeight.Bold) },
                                    textStyle = TextStyle(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp
                                    ),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    colors = TextFieldDefaults.colors(
                                        unfocusedContainerColor = colors.buttonColor,
                                        focusedContainerColor = colors.playerButtonsColor2,
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
                                        focusedPlaceholderColor = colors.calledMoneyColor.copy(alpha = 0.8f)
                                    ),
                                    singleLine = true,
                                    modifier = Modifier
                                        .width(300.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                if (nickname.isNotBlank()){
                                                    navController.navigate(
                                                        PlayerScreen(nickname, selectedAvatar)
                                                    )
                                                    { popUpTo(MenuScreen){inclusive = true} }
                                                }
                                            },
                                            enabled = nickname.isNotBlank(),
                                            colors = IconButtonDefaults.iconButtonColors(
                                                containerColor = Color.Transparent,
                                                contentColor = colors.calledMoneyColor
                                            )
                                        ){
                                            Icon(
                                                Icons.AutoMirrored.Filled.Login,
                                                ""
                                            )
                                        }
                                    }
                                )
                            }

                            AvatarsSelectionView{ selectedAvatar = it }
                        }
                    } else{
                        NoPermissionsView(
                            forServer = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarsSelectionView(
    selectedAvatarCallback: (Int) -> Unit
) {
    val density = LocalDensity.current
    val listOfAvatars = listOf(
        R.raw.avatar_female_5,
        R.raw.avatar_female_1,
        R.raw.avatar_female_2,
        R.raw.avatar_female_3,
        R.raw.avatar_female_4,
        R.raw.avatar_male_1,
        R.raw.avatar_male_2,
        R.raw.avatar_male_3,
        R.raw.avatar_male_4,
        R.raw.avatar_male_5,
    )
    var selectedAvatar by rememberSaveable { mutableIntStateOf(listOfAvatars.first()) }
    LaunchedEffect(selectedAvatar) {
        selectedAvatarCallback(selectedAvatar)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .heightIn(100.dp, 330.dp)
                .padding(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(
                    2.dp,
                    colors.calledMoneyColor,
                    RoundedCornerShape(10.dp)
                )
                .background(colors.playerButtonsColor2.copy(0.8f)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = UiTexts.StringResource(R.string.choose_avatar).asString(),
                modifier = Modifier
                    .padding(10.dp),
                fontWeight = FontWeight.Bold,
                color = colors.calledMoneyColor,
                style = MaterialTheme.typography.titleLarge.copy(
                    textDecoration = TextDecoration.Underline
                )
            )

            val gridState = rememberLazyGridState()
            val gridItemSize = 110.dp
            var itemSize by remember { mutableStateOf(DpSize.Zero) }
            LazyHorizontalGrid(
                state = gridState,
                rows = GridCells.FixedSize(gridItemSize),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .onGloballyPositioned {
                        itemSize = with(density) {
                            DpSize(
                                it.size.width.toDp(),
                                it.size.height.toDp()
                            )
                        }
                    }
            ) {
                itemsIndexed(listOfAvatars) { _, item ->
                    val composition by rememberLottieComposition(
                        spec = LottieCompositionSpec.RawRes(
                            item
                        )
                    )
                    val progress by animateLottieCompositionAsState(
                        composition = composition,
                        iterations = LottieConstants.IterateForever
                    )

                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier
                            .size(
                                if (itemSize.height < gridItemSize) {
                                    itemSize.height
                                } else {
                                    gridItemSize
                                }
                            )
                            .padding(10.dp)
                            .clip(CircleShape)
                            .background(colors.buttonColor)
                            .border(
                                5.dp,
                                if (selectedAvatar == item) Color.Green else colors.calledMoneyColor,
                                CircleShape
                            )
                            .clickable(
                                interactionSource = null,
                                indication = null,
                                onClick = { selectedAvatar = item }
                            ),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CheckPermissions(
    permissionsState: (MultiplePermissionsState) -> Unit
) {
    var permissionsList = listOf<String>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        permissionsList = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.NEARBY_WIFI_DEVICES,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        permissionsList = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
        )
    } else{
        permissionsList = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    val permissions = rememberMultiplePermissionsState(permissions = permissionsList)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(
        key1 = lifecycleOwner,
        effect = {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START) {
                    permissions.launchMultiplePermissionRequest()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    )

    permissionsState(permissions)
}


@Composable
fun NoPermissionsView(
    forServer: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        val composition by rememberLottieComposition(spec = LottieCompositionSpec.RawRes(R.raw.no_permissions))
        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = LottieConstants.IterateForever
        )

        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier
                .size(200.dp)
        )

        val textsArray = UiTexts.ArrayResource(R.array.permissions).asArray()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .border(
                        2.dp,
                        colors.calledMoneyColor,
                        RoundedCornerShape(10.dp)
                    )
                    .background(colors.playerButtonsColor2.copy(0.8f))
                    .padding(8.dp),
            ) {
                Text(
                    text = if (forServer) textsArray[0] else textsArray[1],
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
                Text(
                    text = textsArray[2],
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            val context = LocalContext.current
            FloatingActionButton(
                onClick = {
                    context.startActivity(
                        Intent(
                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            android.net.Uri.fromParts("package", context.packageName, null)
                        )
                    )
                },
                containerColor = colors.playerButtonsColor2,
                contentColor = Color.White
            ) {
                Icon(
                    Icons.Filled.Settings,
                    "settings"
                )
            }
        }
    }
}