package com.dabi.partypoker.featureCore.views

import android.app.LocaleManager
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.zIndex
import androidx.core.app.LocaleManagerCompat
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dabi.partypoker.R
import com.dabi.partypoker.featureCore.data.myColors
import com.dabi.partypoker.featureCore.viewModel.PlayerSettingsEvent
import com.dabi.partypoker.featureCore.viewModel.PlayerSettingsViewModel
import com.dabi.partypoker.ui.theme.textColor
import com.dabi.partypoker.utils.CardsUtils
import com.dabi.partypoker.utils.UiTexts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale


enum class ExpandedSettings{
    NONE,
    ABOUT,
    RULES,
    COMBINATIONS,
    GLOSSARY,
    RESOURCES
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlayerSettings(
    isVisible: Boolean,
    showSettings: (Boolean) -> Unit,
) {
    @Composable
    fun showArrayString(
        arrayId: Int
    ){
        val items = UiTexts.ArrayResource(arrayId).asArray()
        items.forEach { item ->
            val t = item.split(":")
            val t1 = t[0]
            val t2 = t[1]

            val annotatedText = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                    append("$t1: ")
                }
                append(t2)
            }

            Text(
                text = annotatedText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
    @Composable
    fun showArrayLinks(
        arrayId: Int
    ){
        val context = LocalContext.current
        val items = UiTexts.ArrayResource(arrayId).asArray()
        items.forEach { linkString ->
            Text(
                text = linkString,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.tertiary,
                    textDecoration = TextDecoration.Underline
                ),
                modifier = Modifier
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkString))
                        context.startActivity(intent)
                    }
                    .padding(vertical = 4.dp)
            )
        }
    }

    val playerSettingsVM = hiltViewModel<PlayerSettingsViewModel>()
    val playerSettingsState by playerSettingsVM.playerSettingsState.collectAsStateWithLifecycle()

    var expandedSettings by rememberSaveable { mutableStateOf(ExpandedSettings.NONE) }

    val orientation = LocalConfiguration.current.orientation
    Crossfade(
        targetState = isVisible,
        modifier = Modifier
            .zIndex(1f)
    ) {
        if (it) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray.copy(0.6f))
                    .clickable(
                        interactionSource = null,
                        indication = null
                    ) {
                        showSettings(false)
                        expandedSettings = ExpandedSettings.NONE
                    }
            ){
                BackHandler(true) {
                    showSettings(false)
                    expandedSettings = ExpandedSettings.NONE
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f)
    ){
        AnimatedVisibility(
            visible = isVisible,
            modifier = Modifier
                .align(Alignment.BottomEnd),
            enter = slideInHorizontally { it },
            exit = slideOutHorizontally { it }
        ) {
            val state = rememberLazyListState()

            val activeStickyHeader by remember {
                derivedStateOf { state.layoutInfo.visibleItemsInfo.firstOrNull()?.key }
            }
            LazyColumn(
                state = state,
                modifier = Modifier
                    .fillMaxHeight(1f)
                    .fillMaxWidth(0.8f)
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
                stickyHeader(key = "sh1") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Box(modifier = Modifier
                            .minimumInteractiveComponentSize()
                        ){
                            if (activeStickyHeader == "sh1"){
                                IconButton(
                                    onClick = {
                                        showSettings(false)
                                        expandedSettings = ExpandedSettings.NONE
                                    },
                                    modifier = Modifier
                                        .border(
                                            2.dp,
                                            textColor,
                                            CircleShape
                                        )
                                        .minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        "close",
                                        tint = textColor
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.7f)
                                .weight(1f, fill = false),
                            contentAlignment = Alignment.Center
                        ){
                            AutoSizeText(
                                text = UiTexts.StringResource(R.string.player_settings).asString(),
                                style = MaterialTheme.typography.headlineLarge
                            )
                        }

                        // Just to make sure the text is centered
                        Box(modifier = Modifier
                            .minimumInteractiveComponentSize()
                        )
                    }
                }

                item {
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Column(
                            modifier = Modifier
                                .weight(1f, false)
                        ) {
                            val texts = UiTexts.ArrayResource(R.array.vibration).asArray()
                            AutoSizeText(
                                text = texts[0],
                                style = MaterialTheme.typography.headlineLarge
                            )
                            AutoSizeText(
                                text = texts[1],
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Switch(
                            checked = playerSettingsState.vibration,
                            onCheckedChange = { playerSettingsVM.onEvent(PlayerSettingsEvent.ToggleVibration) },
                        )
                    }

                    HorizontalDivider()

                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Column(
                            modifier = Modifier
                                .weight(1f, false)
                        ) {
                            val texts = UiTexts.ArrayResource(R.array.language).asArray()
                            AutoSizeText(
                                text = texts[0],
                                style = MaterialTheme.typography.headlineLarge
                            )
                            AutoSizeText(
                                text = texts[1],
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        val scope = rememberCoroutineScope()
                        var dropdownExpanded by rememberSaveable { mutableStateOf(false) }
                        val languages = mapOf(
                            "en" to UiTexts.ArrayResource(R.array.english).asArray(),
                            "cs" to UiTexts.ArrayResource(R.array.czech).asArray()
                        )
                        ExposedDropdownMenuBox(
                            expanded = dropdownExpanded,
                            onExpandedChange = {
                                dropdownExpanded = !dropdownExpanded
                            },
                            modifier = Modifier
                                .weight(1f, fill = false)
                        ){
                            TextField(
                                value = languages[Locale.getDefault().language]?.get(1) ?: "unknown",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = dropdownExpanded
                                    )
                                },
                                textStyle = TextStyle(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    platformStyle = null
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                colors = TextFieldDefaults.myColors(),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                languages.forEach {
                                    DropdownMenuItem(
                                        text = {
                                            AutoSizeText(
                                                text = it.value[1],
                                                style = MaterialTheme.typography.labelLarge
                                            )
//                                            Text(text = it.value[1])
                                        },
                                        onClick = {
                                            scope.launch(Dispatchers.Main) {
                                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(it.value[0]))
                                            }
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                stickyHeader(key = "sh2") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Box(modifier = Modifier
                            .minimumInteractiveComponentSize()
                        ){
                            if (activeStickyHeader == "sh2"){
                                IconButton(
                                    onClick = {
                                        showSettings(false)
                                        expandedSettings = ExpandedSettings.NONE
                                    },
                                    modifier = Modifier
                                        .border(
                                            2.dp,
                                            textColor,
                                            CircleShape
                                        )
                                        .minimumInteractiveComponentSize()
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        "close",
                                        tint = textColor
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.7f)
                                .weight(1f, fill = false),
                            contentAlignment = Alignment.Center
                        ){
                            AutoSizeText(
                                text = UiTexts.StringResource(R.string.settings_info).asString(),
                                style = MaterialTheme.typography.headlineLarge
                            )
                        }

                        // Just to make sure the text is centered
                        Box(modifier = Modifier
                            .minimumInteractiveComponentSize()
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .clickable(
                                interactionSource = null,
                                indication = null,
                                onClick = {
                                    expandedSettings =
                                        if (expandedSettings != ExpandedSettings.ABOUT) {
                                            ExpandedSettings.ABOUT
                                        } else {
                                            ExpandedSettings.NONE
                                        }
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.weight(1f, false)){
                            AutoSizeText(
                                text = UiTexts.StringResource(R.string.about_game).asString(),
                                style = MaterialTheme.typography.headlineLarge
                            )
                        }
                        Icon(
                            imageVector = if (expandedSettings == ExpandedSettings.ABOUT) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "btn"
                        )
                    }

                    AnimatedVisibility(
                        visible = expandedSettings == ExpandedSettings.ABOUT,
                        enter = expandVertically { -it },
                        exit = shrinkVertically { -it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                    ) {
                        Text(
                            text = UiTexts.StringResource(R.string.info_about_content).asString(),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    HorizontalDivider()
                }

                item {
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .clickable(
                                interactionSource = null,
                                indication = null,
                                onClick = {
                                    expandedSettings =
                                        if (expandedSettings != ExpandedSettings.RULES) {
                                            ExpandedSettings.RULES
                                        } else {
                                            ExpandedSettings.NONE
                                        }
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Box(modifier = Modifier.weight(1f, false)){
                            AutoSizeText(
                                text = UiTexts.StringResource(R.string.rules_title).asString(),
                                style = MaterialTheme.typography.headlineLarge
                            )
                        }
                        Icon(
                            imageVector = if (expandedSettings == ExpandedSettings.RULES) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "btn"
                        )
                    }

                    AnimatedVisibility(
                        visible = expandedSettings == ExpandedSettings.RULES,
                        enter = expandVertically { -it },
                        exit = shrinkVertically { -it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                    ) {
                        Column {
                            AutoSizeText(
                                text = UiTexts.StringResource(R.string.game_goal_title).asString(),
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = UiTexts.StringResource(R.string.game_goal).asString(),
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            AutoSizeText(
                                text = UiTexts.StringResource(R.string.equipment_title).asString(),
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            showArrayString(R.array.equipment_items)

                            Spacer(modifier = Modifier.height(10.dp))
                            AutoSizeText(
                                text = UiTexts.StringResource(R.string.basic_terms_title).asString(),
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            showArrayString(R.array.basic_terms)

                            Spacer(modifier = Modifier.height(10.dp))
                            AutoSizeText(
                                text = UiTexts.StringResource(R.string.game_flow_title).asString(),
                                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            showArrayString(R.array.game_flow)
                        }
                    }

                    HorizontalDivider()
                }

                item {
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .clickable(
                                interactionSource = null,
                                indication = null,
                                onClick = {
                                    expandedSettings =
                                        if (expandedSettings != ExpandedSettings.COMBINATIONS) {
                                            ExpandedSettings.COMBINATIONS
                                        } else {
                                            ExpandedSettings.NONE
                                        }
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Box(modifier = Modifier.weight(1f, false)){
                            AutoSizeText(
                                text = UiTexts.StringResource(R.string.hand_rank_title).asString(),
                                style = MaterialTheme.typography.headlineLarge
                            )
                        }
                        Icon(
                            imageVector = if (expandedSettings == ExpandedSettings.COMBINATIONS) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "btn"
                        )
                    }

                    AnimatedVisibility(
                        visible = expandedSettings == ExpandedSettings.COMBINATIONS,
                        enter = expandVertically { -it },
                        exit = shrinkVertically { -it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                    ) {
                        val texts = UiTexts.ArrayResource(R.array.cards_tooltip).asArray().toMutableList()
                        texts.removeAt(0)
                        val combinations = CardsUtils.handCombinations

                        Column {
                            Text(
                                text = UiTexts.StringResource(R.string.hand_rank_content).asString(),
                                style = MaterialTheme.typography.bodyLarge
                            )

                            texts.forEachIndexed { index, text ->
                                val textRow = text.split(": ")
                                HorizontalDivider(modifier = Modifier.padding(8.dp))
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    AutoSizeText(
                                        text = (index+1).toString() + ". " + textRow[0] + ": ",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(5.dp),
                                        horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)
                                    ) {
                                        combinations.getOrNull(index)?.let {
                                            for (pair in it){
                                                val card = pair.first

                                                CardBox(cardId = card.toImageId(), modifier = Modifier
                                                    .weight(1f, false)
                                                    .alpha(if (pair.second) 1f else 0.5f)
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = textRow[1],
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider()
                }

                item {
                    Row (
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .clickable(
                                interactionSource = null,
                                indication = null,
                                onClick = {
                                    expandedSettings =
                                        if (expandedSettings != ExpandedSettings.GLOSSARY) {
                                            ExpandedSettings.GLOSSARY
                                        } else {
                                            ExpandedSettings.NONE
                                        }
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Box(modifier = Modifier.weight(1f, false)){
                            AutoSizeText(
                                text = UiTexts.StringResource(R.string.glossary_title).asString(),
                                style = MaterialTheme.typography.headlineLarge
                            )
                        }
                        Icon(
                            imageVector = if (expandedSettings == ExpandedSettings.GLOSSARY) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "btn"
                        )
                    }

                    AnimatedVisibility(
                        visible = expandedSettings == ExpandedSettings.GLOSSARY,
                        enter = expandVertically { -it },
                        exit = shrinkVertically { -it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                    ) {
                        Column {
                            showArrayString(R.array.glossary_terms)
                        }
                    }
                    HorizontalDivider()
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .clickable(
                                interactionSource = null,
                                indication = null,
                                onClick = {
                                    expandedSettings =
                                        if (expandedSettings != ExpandedSettings.RESOURCES) {
                                            ExpandedSettings.RESOURCES
                                        } else {
                                            ExpandedSettings.NONE
                                        }
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.weight(1f, false)){
                            AutoSizeText(
                                text = UiTexts.StringResource(R.string.resources_title).asString(),
                                style = MaterialTheme.typography.headlineLarge
                            )
                        }
                        Icon(
                            imageVector = if (expandedSettings == ExpandedSettings.RESOURCES) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "btn"
                        )
                    }

                    AnimatedVisibility(
                        visible = expandedSettings == ExpandedSettings.RESOURCES,
                        enter = expandVertically { -it },
                        exit = shrinkVertically { -it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                    ) {
                        Column {
                            Text(
                                text = UiTexts.StringResource(R.string.resources_content).asString(),
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            showArrayLinks(R.array.resources_links)
                        }
                    }
                }
            }
        }
    }
}
