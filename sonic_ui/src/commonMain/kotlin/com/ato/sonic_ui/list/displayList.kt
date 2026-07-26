package com.ato.sonic_ui.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ato.sonic_ui.base.empty.EmptyState
import com.ato.sonic_ui.base.skeleton.SkeletonCard
import com.ato.ui_state.base.list.UiList
import org.jetbrains.compose.resources.stringResource

/**
 * Список с тремя состояниями: загрузка, пусто, данные.
 *
 * Загрузка — заглушки в форме будущих карточек, а не строка «Loading»; пусто —
 * центрированный блок, которому можно передать действие. Оба состояния
 * заменяются своими через [skeleton] и [emptyContent], поэтому экраны с
 * нестандартной карточкой не обязаны мириться с дефолтом.
 */
fun <T> LazyListScope.displayList(
    uiList: UiList<T>,
    modifier: Modifier = Modifier,
    skeletonCount: Int = 3,
    skeleton: (@Composable (Modifier) -> Unit)? = null,
    emptyContent: (@Composable (Modifier) -> Unit)? = null,
    listContent: @Composable (T, Modifier) -> Unit,
) {
    if (!uiList.isVisible) return

    val list = uiList.content
    uiList.contentTitle?.let { contentTitle ->
        item {
            Text(
                text = stringResource(contentTitle),
                modifier = modifier,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
    if (list == null) {
        // Загрузку озвучиваем один раз на весь блок — иначе скринридер прочитал
        // бы каждую заглушку по отдельности.
        item {
            val loadingText = stringResource(uiList.loading)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = loadingText }
            ) {
                repeat(skeletonCount) { index ->
                    if (skeleton != null) {
                        skeleton(modifier)
                    } else {
                        SkeletonCard(modifier = modifier.fillMaxWidth())
                    }
                    if (index != skeletonCount - 1) {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    } else {
        if (list.isEmpty()) {
            item {
                if (emptyContent != null) {
                    emptyContent(modifier)
                } else {
                    EmptyState(
                        text = stringResource(uiList.empty),
                        modifier = modifier,
                    )
                }
            }
        } else {
            items(list) { item ->
                listContent(item, modifier)
            }
        }
    }
}

@Composable
fun <T> DisplayListAsRow(
    uiList: UiList<T>,
    modifier: Modifier = Modifier,
    containerModifier: Modifier = Modifier,
    skeletonCount: Int = 3,
    emptyContent: (@Composable (Modifier) -> Unit)? = null,
    listContent: @Composable (T, Modifier) -> Unit,
) {
    if (!uiList.isVisible) return

    val list = uiList.content
    Column(containerModifier) {
        uiList.contentTitle?.let { contentTitle ->
            Text(
                text = stringResource(contentTitle),
                modifier = modifier,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (list == null) {
            val loadingText = stringResource(uiList.loading)
            LazyRow(
                modifier = Modifier.semantics { contentDescription = loadingText }
            ) {
                items(skeletonCount) {
                    SkeletonCard(modifier = modifier)
                }
            }
        } else {
            if (list.isEmpty()) {
                if (emptyContent != null) {
                    emptyContent(modifier)
                } else {
                    EmptyState(
                        text = stringResource(uiList.empty),
                        modifier = modifier,
                    )
                }
            } else {
                LazyRow {
                    items(list) { item ->
                        listContent(item, modifier)
                    }
                }
            }
        }
    }
}
