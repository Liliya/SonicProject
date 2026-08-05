package com.ato.sonic_ui.wishlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ato.sonic_ui.base.image.DisplayImage
import com.ato.ui_state.base.image.UiImagePicker

@Composable
fun PersonCard(
    name: String,
    nick: String,
    avaUrl: String?,
    onClick: (() -> Unit)? = null,
    colors: CardColors = CardDefaults.cardColors(),
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = colors,
        onClick = { onClick?.invoke() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .padding(top = 8.dp)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "@$nick",
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .padding(bottom = 8.dp)
                )
            }
            // Раньше кружок рисовался только под `avaUrl != null`, поэтому у
            // людей без фотографии карточка была голой, а у остальных нет —
            // и список ехал по вертикали. Со встроенным пресетом аватарка есть
            // всегда, и ряд карточек наконец одной высоты.
            Box(
                modifier = Modifier.padding(8.dp).padding(end = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                DisplayImage(
                    imagePikerState = UiImagePicker(avaUrl),
                    size = 64f,
                    avatarSeed = nick,
                    onImageClicked = onClick ?: {}
                )
            }
        }
    }
}