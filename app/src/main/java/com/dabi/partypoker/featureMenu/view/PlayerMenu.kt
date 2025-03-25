package com.dabi.partypoker.featureMenu.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dabi.easylocalgame.composeUtils.UiTexts
import com.dabi.partypoker.MenuScreen
import com.dabi.partypoker.PlayerScreen
import com.dabi.partypoker.R
import com.dabi.partypoker.featureCore.data.myColors
import com.dabi.partypoker.ui.theme.textColor


@Composable
fun PlayerMenuView(
    navController: NavController
) {
    var nickname by rememberSaveable { mutableStateOf("") }

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
                colors = TextFieldDefaults.myColors(),
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
                            contentColor = textColor
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
}