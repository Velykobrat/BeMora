package com.bebetter.bemora.ui.components

import androidx.compose.ui.res.stringResource
import com.bebetter.bemora.R
import com.bebetter.bemora.ui.components.localizedLabel
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.bebetter.bemora.domain.model.ContentType

@Composable
fun CatalogAttribution(types: List<ContentType>) {
    val uriHandler = LocalUriHandler.current
    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (ContentType.BOOK in types) {
            TextButton(onClick = { uriHandler.openUri("https://openlibrary.org") }) { Text(stringResource(R.string.books_open_library)) }
        }
        if (ContentType.GAME in types) {
            TextButton(onClick = { uriHandler.openUri("https://rawg.io") }) { Text(stringResource(R.string.games_rawg)) }
        }
    }
}
