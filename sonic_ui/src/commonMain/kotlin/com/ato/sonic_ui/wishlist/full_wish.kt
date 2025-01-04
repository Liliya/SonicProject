package com.ato.sonic_ui.wishlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.ato.sonic_ui.base.image.DisplayImage
import com.ato.ui_state.base.image.UiImagePicker
import com.ato.ui_state.wishlist.WishlistWish


@Composable
fun DisplayFullWish(
    wish: WishlistWish,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            if (!wish.imageUrl.isNullOrEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    DisplayImage(
                        imagePikerState = UiImagePicker(wish.imageUrl),
                        shape = RoundedCornerShape(16.dp),
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            wish.name?.let { name ->
                SelectionContainer {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            if (!wish.description.isNullOrEmpty()) {
                SelectionContainer {
                    Text(
                        text = wish.description.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            if (!wish.url.isNullOrEmpty()) {
                SelectionContainer {
                    ClickableUrlText(
                        url = wish.url!!,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ClickableUrlText(url: String, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current

    val annotatedText = buildAnnotatedString {
        pushStringAnnotation(tag = "URL", annotation = url)
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.error)) {
            append(url)
        }
        pop()
    }

    ClickableText(
        text = annotatedText,
        onClick = { offset ->
            annotatedText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    try {
                        uriHandler.openUri(annotation.item)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        },
        modifier = modifier
    )
}