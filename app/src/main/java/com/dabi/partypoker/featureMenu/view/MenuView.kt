package com.dabi.partypoker.featureMenu.view

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
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
import com.dabi.partypoker.R
import com.dabi.partypoker.featureCore.viewModel.PlayerSettingsViewModel
import com.dabi.partypoker.featureCore.views.AutoSizeText
import com.dabi.partypoker.featureCore.views.PlayerSettings
import com.dabi.partypoker.ui.theme.textColor
import com.dabi.partypoker.utils.UiTexts
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState


enum class ViewPosition{
    MENU,
    LOCAL_SERVER,
    LOCAL_PLAYER
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MenuView(
    navController: NavController
) {
    var viewPosition by rememberSaveable { mutableStateOf(ViewPosition.MENU) }

    var permissions: MultiplePermissionsState = rememberMultiplePermissionsState(emptyList())
    CheckPermissions(permissionsState = { permissions = it })
    BackHandler(true) {  }

    @Composable
    fun backToMenu(){
        Button(
            onClick = {
                viewPosition = ViewPosition.MENU
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = textColor
            ),
            contentPadding = PaddingValues(5.dp, vertical = 0.dp),
            modifier = Modifier
                .padding(16.dp)
                .border(2.dp, textColor, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer.copy(0.8f))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    "back"
                )
                Text(UiTexts.StringResource(R.string.menu).asString())
            }
        }
    }

    Crossfade(
        targetState = viewPosition,
        modifier = Modifier
            .paint(
                painterResource(id = R.drawable.game_start_background),
                contentScale = ContentScale.Crop
            )
            .fillMaxSize(),
    ) { position ->
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
                        shape = RoundedCornerShape(10.dp)
                    ){
                        AutoSizeText(
                            text = UiTexts.StringResource(R.string.connect_local).asString(),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }


                var showSettings by rememberSaveable { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.TopStart
                ){
                    FloatingActionButton(
                        modifier = Modifier
                            .width(50.dp)
                            .height(50.dp),
                        onClick = {
                            showSettings = true
                        },
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
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
                PlayerSettings(
                    isVisible = showSettings,
                    showSettings = { showSettings = it },
                )
            }
            ViewPosition.LOCAL_SERVER -> {
                BackHandler(true) {
                    viewPosition = ViewPosition.MENU
                }
                backToMenu()

                if (permissions.allPermissionsGranted) {
                    ServerMenuView(navController = navController)
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
                backToMenu()

                if (permissions.allPermissionsGranted) {
                    PlayerMenuView(navController = navController)
                } else{
                    NoPermissionsView(
                        forServer = false
                    )
                }
            }
        }
    }
}

@Composable
fun AvatarsSelectionView(
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
                    textColor,
                    RoundedCornerShape(10.dp)
                )
                .background(MaterialTheme.colorScheme.surfaceContainer.copy(0.8f)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = UiTexts.StringResource(R.string.choose_avatar).asString(),
                modifier = Modifier
                    .padding(10.dp),
                fontWeight = FontWeight.Bold,
                color = textColor,
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
                            .background(MaterialTheme.colorScheme.tertiaryContainer)
                            .border(
                                5.dp,
                                if (selectedAvatar == item) Color.Green else textColor,
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
    val permissionsList = mutableListOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
    )
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R){
        permissionsList.add(Manifest.permission.BLUETOOTH)
        permissionsList.add(Manifest.permission.BLUETOOTH_ADMIN)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
        permissionsList.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        permissionsList.add(Manifest.permission.BLUETOOTH_CONNECT)
        permissionsList.add(Manifest.permission.BLUETOOTH_SCAN)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
        permissionsList.add(Manifest.permission.NEARBY_WIFI_DEVICES)
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
            .fillMaxSize()
            .padding(16.dp),
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
                        textColor,
                        RoundedCornerShape(10.dp)
                    )
                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(0.8f))
                    .padding(8.dp)
                    .weight(1f, false),
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
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
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


