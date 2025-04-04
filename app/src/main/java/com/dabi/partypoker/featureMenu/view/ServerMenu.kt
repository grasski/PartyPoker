package com.dabi.partypoker.featureMenu.view

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Start
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dabi.easylocalgame.serverSide.data.ServerType
import com.dabi.easylocalgame.composeUtils.UiTexts
import com.dabi.partypoker.MenuScreen
import com.dabi.partypoker.R
import com.dabi.partypoker.ServerScreen
import com.dabi.partypoker.featureCore.data.myColors
import com.dabi.partypoker.featureCore.data.myColorsSettings
import com.dabi.partypoker.featureCore.views.AutoSizeText
import com.dabi.partypoker.featureCore.views.formatNumberToString
import com.dabi.partypoker.featureMenu.viewModel.MenuGameSettingsEvent
import com.dabi.partypoker.featureMenu.viewModel.MenuViewModel
import com.dabi.partypoker.featureMenu.viewModel.NewSettingsEvent
import com.dabi.partypoker.featureMenu.viewModel.SettingsError
import com.dabi.partypoker.repository.gameSettings.GameSettings
import com.dabi.partypoker.ui.theme.textColor
import kotlin.math.roundToInt


@Composable
fun ServerMenuView(
    navController: NavController,
) {
    val density = LocalDensity.current

    var nickname by rememberSaveable { mutableStateOf("") }
    var localServerPlaying by rememberSaveable { mutableStateOf(false) }

    var showSettings by rememberSaveable { mutableStateOf(false) }
    var serverGameSettingsId by rememberSaveable { mutableLongStateOf(0) }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val maxHeight = this.constraints.maxHeight
        var textFieldSize by remember { mutableStateOf(DpSize.Zero) }
        var heightOfCenter by remember { mutableStateOf(IntSize.Zero) }
        var selectedAvatar by rememberSaveable { mutableIntStateOf(0) }

        val animateOffset = remember {
            Animatable(
                initialValue = if (localServerPlaying)
                    (maxHeight / 2).minus(heightOfCenter.height).toFloat()
                        .minus(with(density) { 20.dp.toPx() })
                else
                    (maxHeight / 2).minus(heightOfCenter.height.div(2)).toFloat()
            )
        }
        LaunchedEffect(localServerPlaying) {
            animateOffset.animateTo(
                if (localServerPlaying)
                    (maxHeight / 2).minus(heightOfCenter.height).toFloat()
                        .minus(with(density) { 20.dp.toPx() })
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
                    .padding(start = 55.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .height(55.dp)
                        .border(
                            2.dp,
                            textColor,
                            RoundedCornerShape(10.dp)
                        )
                        .background(MaterialTheme.colorScheme.surfaceContainer.copy(0.8f))
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
                ) {
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
                            checkedColor = textColor,
                            uncheckedColor = Color.White
                        )
                    )
                }

                FloatingActionButton(
                    modifier = Modifier
                        .width(55.dp)
                        .height(55.dp)
                        .padding(5.dp),
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

            Crossfade(
                targetState = localServerPlaying,
            ) { isPlaying ->
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
                        colors = TextFieldDefaults.myColors(),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (nickname.isNotBlank()){
                                        navController.navigate(
                                            ServerScreen(
                                                serverType = ServerType.IS_PLAYER.toString(),
                                                serverName = nickname.ifEmpty { "ServerOwner" },
                                                avatarId = selectedAvatar,
                                                serverGameSettingsId = serverGameSettingsId
                                            )
                                        ) {
                                            popUpTo(MenuScreen){inclusive = true}
                                        }
                                    }
                                },
                                enabled = nickname.isNotBlank(),
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = textColor
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
                                        avatarId = 0,
                                        serverGameSettingsId = serverGameSettingsId
                                    )
                                ) {
                                    popUpTo(MenuScreen){inclusive = true}
                                }
                            },
                            modifier = Modifier
                                .fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 5.dp),
                            shape = RoundedCornerShape(10.dp)
                        ){
                            AutoSizeText(
                                text = UiTexts.StringResource(R.string.server_start).asString(),
                                style = MaterialTheme.typography.titleLarge
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
        Crossfade(targetState = showSettings) {
            if (it) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.DarkGray.copy(0.6f))
                        .clickable(
                            interactionSource = null,
                            indication = null
                        ) {
                            showSettings = false
                        }
                )
            }
        }
        AnimatedVisibility(
            visible = showSettings,
            modifier = Modifier
                .align(Alignment.BottomEnd),
            enter = if (orientation == Configuration.ORIENTATION_PORTRAIT) slideInVertically { it } else slideInHorizontally { it },
            exit = if (orientation == Configuration.ORIENTATION_PORTRAIT) slideOutVertically { it } else slideOutHorizontally { it }
        ) {
            ServerSettingsView(
                orientation,
                showSettings = { showSettings = it },
                selectedSettingsId = { serverGameSettingsId = it }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSettingsView(
    orientation: Int,
    showSettings: (Boolean) -> Unit,
    selectedSettingsId: (Long) -> Unit
) {
    val menuViewModel: MenuViewModel = hiltViewModel()
    val gameSettings by menuViewModel.gameSettings.collectAsStateWithLifecycle()
    val selectedSetting by menuViewModel.selectedSetting.collectAsStateWithLifecycle()
    LaunchedEffect(selectedSetting) {
        selectedSettingsId(selectedSetting.id)
    }

    var openSettings by rememberSaveable { mutableStateOf(false) }
    var isUpdating by rememberSaveable { mutableStateOf(false) }
    val newSettings by menuViewModel.newSettings.collectAsStateWithLifecycle()
    val settingsErrors by menuViewModel.errors.collectAsStateWithLifecycle()

    if (openSettings){
        UpsertSettingsDialog(
            settings = newSettings,
            errors = settingsErrors,
            update = isUpdating,
            dismiss = { fromSave ->
                if (!fromSave){
                    openSettings = false
                }

                if (settingsErrors.isEmpty()){
                    openSettings = false
                }
            },
            newSettingEvent = menuViewModel::onChangeEvent
        )
    }

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
        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        .padding(8.dp),
    ){
        item{
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ){
                IconButton(
                    onClick = {
                        showSettings(false)
                    },
                    modifier = Modifier
                        .border(
                            2.dp,
                            textColor,
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Filled.Close,
                        "close",
                        tint = textColor,
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.7f)
                        .weight(1f, false),
                    contentAlignment = Alignment.Center
                ){
                    AutoSizeText(
                        text = UiTexts.StringResource(R.string.game_settings).asString(),
                        style = MaterialTheme.typography.headlineLarge
                    )
                }

                IconButton(
                    onClick = {
                        isUpdating = false
                        openSettings = true
                    },
                    modifier = Modifier
                        .border(
                            2.dp,
                            textColor,
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Filled.AddCircle,
                        "create",
                        tint = textColor,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        item {
            var dropdownExpanded by rememberSaveable { mutableStateOf(false) }
            gameSettings.map { it.title }

            Row(
                modifier = Modifier
                    .fillParentMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ){
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = {
                        if (gameSettings.size > 1) {
                            dropdownExpanded = !dropdownExpanded
                        }
                    },
                    modifier = Modifier
                        .weight(1f, fill = false)
                ){
                    TextField(
                        modifier = Modifier
                            .menuAnchor(),
                        value = if (selectedSetting.isDefault) UiTexts.StringResource(R.string.default_settings).asString() else selectedSetting.title,
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
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        colors = TextFieldDefaults.myColors(),
                        shape = RoundedCornerShape(10.dp),
                    )

                    if (gameSettings.size > 1){
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            gameSettings.forEach {
                                DropdownMenuItem(
                                    text = {
                                        if (it.isDefault){
                                            Text(text = UiTexts.StringResource(R.string.default_settings).asString())
                                        } else {
                                            Text(it.title)
                                        }
                                    },
                                    onClick = {
                                        menuViewModel.onEvent(MenuGameSettingsEvent.SelectSettings(it))
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                if (!selectedSetting.isDefault){
                    IconButton(
                        onClick = {
                            menuViewModel.onChangeEvent(NewSettingsEvent.ChangeUpdateToNew(selectedSetting))
                            isUpdating = true
                            openSettings = true
                        },
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            "edit"
                        )
                    }

                    IconButton(
                        onClick = {
                            menuViewModel.onEvent(MenuGameSettingsEvent.DeleteSettings(selectedSetting))
                        },
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            "delete"
                        )
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
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.7f)
                ) {
                    AutoSizeText(
                        text = UiTexts.StringResource(R.string.game_settings_player_money)
                            .asString() + ": ",
                        style = MaterialTheme.typography.displaySmall.copy(
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
                AutoSizeText(text = selectedSetting.playerMoney.formatNumberToString(), style = MaterialTheme.typography.displaySmall)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ){
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.7f)
                ) {
                    AutoSizeText(
                        text = UiTexts.StringResource(R.string.game_settings_small_blind_amount)
                            .asString() + ": ",
                        style = MaterialTheme.typography.displaySmall.copy(
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
                AutoSizeText(text = selectedSetting.smallBlindAmount.formatNumberToString(), style = MaterialTheme.typography.displaySmall)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ){
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.7f)
                ) {
                    AutoSizeText(
                        text = UiTexts.StringResource(R.string.game_settings_big_blind_amount)
                            .asString() + ": ",
                        style = MaterialTheme.typography.displaySmall.copy(
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
                AutoSizeText(text = selectedSetting.bigBlindAmount.formatNumberToString(), style = MaterialTheme.typography.displaySmall)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ){
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.7f)
                ){
                    AutoSizeText(
                        text = UiTexts.StringResource(R.string.game_settings_player_timer_duration).asString() + ": ",
                        style = MaterialTheme.typography.displaySmall.copy(
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
                Box(modifier = Modifier.weight(1f, fill = false)){
                    AutoSizeText(text = selectedSetting.playerTimerDurationMillis.div(1000).toString(), style = MaterialTheme.typography.displaySmall)
                }
            }
        }
    }
}


@Composable
private fun UpsertSettingsDialog (
    settings: GameSettings,
    errors: List<SettingsError>,
    update: Boolean,
    dismiss: (fromSave: Boolean) -> Unit = {},
    newSettingEvent: (NewSettingsEvent) -> Unit
) {
    if (!update){
        LaunchedEffect(Unit) {
            newSettingEvent(NewSettingsEvent.Reset)
        }
    }

    val orientation = LocalConfiguration.current.orientation

    val dialogProperties by remember { mutableStateOf(DialogProperties(
        dismissOnBackPress = false,
        dismissOnClickOutside = false
    )) }
    Dialog(
        onDismissRequest = { dismiss(false) },
        properties = dialogProperties
    ) {
        Card(
            modifier = Modifier
                .fillMaxHeight(if (orientation == Configuration.ORIENTATION_PORTRAIT) 0.7f else 1f)
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .padding(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ){
                AutoSizeText(
                    text = if(update) UiTexts.StringResource(R.string.game_settings_update).asString() else UiTexts.StringResource(R.string.game_settings_new).asString(),
                    style = MaterialTheme.typography.titleLarge,
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
            ) {
                item {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth(0.75f),
                        value = settings.title,
                        onValueChange = { newSettingEvent(NewSettingsEvent.ChangeTitle(it)) },
                        isError = errors.contains(SettingsError.EMPTY_TITLE),
                        label = {
                            Text(text = UiTexts.StringResource(R.string.game_settings_title).asString())
                        },
                        textStyle = TextStyle(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            platformStyle = null
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        colors = TextFieldDefaults.myColorsSettings(),
                        shape = RoundedCornerShape(10.dp),
                    )
                }
                item {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth(0.75f),
                        value = settings.playerMoney.takeIf { it > 0 }?.toString() ?: "",
                        onValueChange = {
                            if (it.toIntOrNull() != null || it.isEmpty()){
                                newSettingEvent(NewSettingsEvent.ChangePlayerMoney(it.toIntOrNull()))
                            }
                        },
                        isError = errors.contains(SettingsError.INVALID_PLAYER_MONEY) || errors.contains(SettingsError.BIG_BLIND_GREATER_THAN_PLAYER_MONEY),
                        label = {
                            Text(text = UiTexts.StringResource(R.string.game_settings_player_money).asString())
                        },
                        textStyle = TextStyle(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            platformStyle = null
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Decimal
                        ),
                        colors = TextFieldDefaults.myColorsSettings(),
                        shape = RoundedCornerShape(10.dp),
                    )
                }
                item {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth(0.75f),
                        value = settings.smallBlindAmount.takeIf { it > 0 }?.toString() ?: "",
                        onValueChange = {
                            if (it.toIntOrNull() != null || it.isEmpty()){
                                newSettingEvent(NewSettingsEvent.ChangeSmallBlindAmount(it.toIntOrNull()))
                            }
                        },
                        isError = errors.contains(SettingsError.INVALID_SMALL_BLIND) || errors.contains(SettingsError.SMALL_BLIND_GREATER_THAN_BIG_BLIND),
                        label = {
                            Text(text = UiTexts.StringResource(R.string.game_settings_small_blind_amount).asString())
                        },
                        textStyle = TextStyle(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            platformStyle = null
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Decimal
                        ),
                        colors = TextFieldDefaults.myColorsSettings(),
                        shape = RoundedCornerShape(10.dp),
                    )
                }
                item {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth(0.75f),
                        value = settings.bigBlindAmount.takeIf { it > 0 }?.toString() ?: "",
                        onValueChange = {
                            if (it.toIntOrNull() != null || it.isEmpty()){
                                newSettingEvent(NewSettingsEvent.ChangeBigBlindAmount(it.toIntOrNull()))
                            }
                        },
                        isError = (
                                errors.contains(SettingsError.INVALID_BIG_BLIND) ||
                                errors.contains(SettingsError.SMALL_BLIND_GREATER_THAN_BIG_BLIND) ||
                                errors.contains(SettingsError.BIG_BLIND_GREATER_THAN_PLAYER_MONEY)
                        ),
                        label = {
                            Text(text = UiTexts.StringResource(R.string.game_settings_big_blind_amount).asString())
                        },
                        textStyle = TextStyle(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            platformStyle = null
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Decimal
                        ),
                        colors = TextFieldDefaults.myColorsSettings(),
                        shape = RoundedCornerShape(10.dp),
                    )
                }
                item {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth(0.75f),
                        value = settings.playerTimerDurationMillis.div(1000).takeIf { it > 0 }?.toString() ?: "",
                        onValueChange = {
                            if (it.toIntOrNull() != null || it.isEmpty()){
                                newSettingEvent(NewSettingsEvent.ChangePlayerTimeout(it.toIntOrNull()))
                            }
                        },
                        isError = errors.contains(SettingsError.INVALID_PLAYER_TIMER),
                        label = {
                            Text(text = UiTexts.StringResource(R.string.game_settings_player_timer_duration).asString())
                        },
                        textStyle = TextStyle(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            platformStyle = null
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Decimal
                        ),
                        colors = TextFieldDefaults.myColorsSettings(),
                        shape = RoundedCornerShape(10.dp),
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Button(
                    onClick = {
                        dismiss(false)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text(UiTexts.StringResource(R.string.cancel).asString())
                }

                Button(
                    onClick = {
                        newSettingEvent(NewSettingsEvent.SaveSettings)
                        dismiss(true)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text(UiTexts.StringResource(R.string.save).asString())
                }
            }
        }
    }
}