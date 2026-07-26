package com.ato.sonic_ui.base.skeleton

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Заглушки на время загрузки.
 *
 * До этого загрузка была строкой текста «Loading» — экран выглядел сломанным,
 * а не занятым, и высота списка прыгала, когда данные приходили. Заглушка
 * повторяет форму будущей карточки, поэтому подстановка данных не двигает
 * вёрстку.
 */
@Composable
private fun skeletonAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "skeleton_alpha",
    )
    return alpha
}

/**
 * Каркас целого экрана: заголовок и несколько карточек.
 *
 * Нужен на те доли секунды, пока `uiState` ещё `null`. Раньше экраны в этот
 * момент рисовали ничего — на холодном старте это читалось как вспышка пустоты.
 *
 * @param loadingLabel что произнесёт скринридер; блок озвучивается целиком
 */
@Composable
fun ScreenSkeleton(
    modifier: Modifier = Modifier,
    cardCount: Int = 3,
    loadingLabel: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .then(
                if (loadingLabel == null) {
                    Modifier
                } else {
                    Modifier.semantics { contentDescription = loadingLabel }
                }
            )
    ) {
        Spacer(Modifier.height(24.dp))
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth(0.45f)
                .height(28.dp),
            shape = MaterialTheme.shapes.small,
        )
        Spacer(Modifier.height(24.dp))
        repeat(cardCount) {
            SkeletonCard(modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
        }
    }
}

/** Прямоугольник-заглушка: строка текста, миниатюра, аватар. */
@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraSmall,
) {
    Box(
        modifier = modifier
            .alpha(skeletonAlpha())
            .background(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                shape = shape,
            )
    )
}

/**
 * Заглушка карточки списка: кружок слева, две строки текста, действие справа —
 * та же раскладка, что у карточки доски и карточки желания.
 */
@Composable
fun SkeletonCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = CardDefaults.outlinedCardBorder(),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkeletonBlock(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(16.dp)
                )
                Spacer(Modifier.height(8.dp))
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth(0.3f)
                        .height(12.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            SkeletonBlock(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.small,
            )
        }
    }
}
