package com.ato.sonic_ui.base.text

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ato.sonic_ui.base.button.DisplayButton
import com.ato.sonic_ui.base.button.DisplayUiIconButton
import com.ato.sonic_ui.base.line.HorizontalSpaceLine
import com.ato.ui_state.Button
import com.ato.ui_state.base.button.UiButton
import com.ato.ui_state.base.button.UiIconButton
import com.ato.ui_state.base.text.UiTitle
import org.jetbrains.compose.resources.stringResource


@Composable
fun LazyItemScope.DisplayTitle(
    state: UiTitle,
    onClick: () -> Unit = {},
    onBackClicked: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Spacer(modifier = Modifier.height(24.dp))
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth(),
    ) {
        val title = if (state.titleFormatArgs == null) {
            stringResource(state.title)
        } else {
            stringResource(state.title, state.titleFormatArgs!!)
        }

        Row(
            modifier = Modifier
                .padding(end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBackClicked != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(
                            onClick = onBackClicked,
                        )
                        .padding(8.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Spacer(modifier = Modifier.width(16.dp))
            }

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 36.sp,
            )
        }

        state.button?.let {
            when (it) {
                is UiButton -> DisplayButton(
                    state = it,
                    onClick = onClick,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                )

                is UiIconButton -> DisplayUiIconButton(
                    state = it,
                    onClick = onClick,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                )
            }

        }
    }
    HorizontalSpaceLine(
        spaceTop = 8.dp,
        line = 1.dp,
        lineColor = MaterialTheme.colorScheme.primary,
        spaceBottom = 16.dp
    )
}

@Composable
fun ColumnScope.DisplayTitle(
    state: UiTitle,
    onClick: () -> Unit = {},
    onBackClicked: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Spacer(modifier = Modifier.height(24.dp))
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth(),
    ) {
        val title = if (state.titleFormatArgs == null) {
            stringResource(state.title)
        } else {
            stringResource(state.title, state.titleFormatArgs!!)
        }

        Row(
            modifier = Modifier
                .padding(end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBackClicked != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable(
                            onClick = onBackClicked,
                        )
                        .padding(8.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else {
                Spacer(modifier = Modifier.width(16.dp))
            }

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 36.sp,
            )
        }

        state.button?.let {
            when (it) {
                is UiButton -> DisplayButton(
                    state = it,
                    onClick = onClick,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                )

                is UiIconButton -> DisplayUiIconButton(
                    state = it,
                    onClick = onClick,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                )
            }

        }
    }
    HorizontalSpaceLine(
        spaceTop = 8.dp,
        line = 1.dp,
        lineColor = MaterialTheme.colorScheme.primary,
        spaceBottom = 16.dp
    )
}

@Composable
fun DisplayAppBarTitle(
    state: UiTitle,
    onClick: () -> Unit = {},
    onBackClicked: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    DisplayTitleUnified(
        title = stringResource(state.title),
        onClick = onClick,
        onBackClicked = onBackClicked,
        button = state.button
    )
}

@Composable
fun DisplayTitleUnified(
    title: String,
    onClick: () -> Unit = {},
    onBackClicked: (() -> Unit)? = null,
    button: Button? = null,
    modifier: Modifier = Modifier
) {
    Spacer(modifier = Modifier.height(24.dp))
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        if (onBackClicked != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBackClicked)
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        button?.let {
            when (it) {
                is UiButton -> DisplayButton(state = it, onClick = onClick)
                is UiIconButton -> DisplayUiIconButton(state = it, onClick = onClick)
            }
        }
    }

    HorizontalSpaceLine(
        spaceTop = 8.dp,
        line = 0.5.dp,
        lineColor = MaterialTheme.colorScheme.outlineVariant,
        spaceBottom = 16.dp
    )
}
