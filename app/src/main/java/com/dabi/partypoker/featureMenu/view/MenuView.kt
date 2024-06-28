package com.dabi.partypoker.featureMenu.view

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dabi.partypoker.MenuScreen
import com.dabi.partypoker.PlayerScreen
import com.dabi.partypoker.R
import com.dabi.partypoker.ServerScreen
import com.dabi.partypoker.featureClient.viewmodel.PlayerViewModel
import com.dabi.partypoker.featureServer.viewmodel.ServerOwnerViewModel
import com.dabi.partypoker.managers.ClientEvents
import com.dabi.partypoker.managers.ServerEvents
import com.dabi.partypoker.managers.ServerType
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
    val context = LocalContext.current
    var viewPosition by rememberSaveable { mutableStateOf(ViewPosition.MENU) }
    var nickname by rememberSaveable { mutableStateOf("") }

    var permissions: MultiplePermissionsState = rememberMultiplePermissionsState(emptyList())
    CheckPermissions(permissionsState = { permissions = it })
    BackHandler(true) {  }

    Crossfade(
        targetState = viewPosition,
        modifier = Modifier
            .paint(
                painterResource(id = R.drawable.game_start_background),
                contentScale = ContentScale.Crop
            )
    ) {position ->
        when (position) {
            ViewPosition.MENU -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = {
                        viewPosition = ViewPosition.LOCAL_SERVER
                    }) {
                        Text(context.getString(R.string.create_local))
                    }
                    Button(onClick = {
                        viewPosition = ViewPosition.LOCAL_PLAYER
                    }) {
                        Text(context.getString(R.string.connect_local))
                    }
                }
            }
            ViewPosition.LOCAL_SERVER -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        BackHandler(true) {
                            viewPosition = ViewPosition.MENU
                        }

                        if (permissions.allPermissionsGranted) {
                            var localServerPlaying by rememberSaveable { mutableStateOf(false) }
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Are you playing?")
                                    Checkbox(
                                        checked = localServerPlaying,
                                        onCheckedChange = { localServerPlaying = it }
                                    )
                                }
                                Crossfade(targetState = localServerPlaying) { isPlaying ->
                                    if (isPlaying) {
                                        TextField(
                                            value = nickname,
                                            onValueChange = { nickname = it }
                                        )
                                    }
                                }

                                Button(
                                    onClick = { navController.navigate(ServerScreen(
                                        serverType = if(localServerPlaying) ServerType.IS_PLAYER.toString() else ServerType.IS_TABLE.toString(),
                                        serverName = nickname.ifEmpty { "ServerOwner" }
                                    )) {
                                        popUpTo(MenuScreen){inclusive = true}
                                    } }
                                ){
                                    Text(text = "START SERVER")
                                }
                            }
                        } else{
                            Text("Not possible to Host a game without accepting permissions")
                        }
                    }
                }
            }
            ViewPosition.LOCAL_PLAYER -> {
                BackHandler(true) {
                    viewPosition = ViewPosition.MENU
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        if (permissions.allPermissionsGranted) {
                            TextField(
                                value = nickname,
                                onValueChange = { nickname = it },
                                placeholder = { Text("Enter your nick name.") },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                            )

                            Button(onClick = {
                                if (nickname.isNotBlank()){
                                    navController.navigate(
                                        PlayerScreen(nickname)
                                    )
                                    { popUpTo(MenuScreen){inclusive = true} }
                                }
                            }
                            ){
                                Text("Login")
                            }
                        } else{
                            Text("Not possible to Join a game without accepting permissions")
                        }
                    }
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