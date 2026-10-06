package com.bebetter.bemora.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bebetter.bemora.domain.model.ContentItem

@Composable
fun SearchResultCard(
    item: ContentItem
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(
                    width = 90.dp,
                    height = 130.dp
                )
                .clip(RoundedCornerShape(12.dp))
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            item.subtitle?.let { subtitle ->
                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = buildString {
                    item.releaseYear?.let {
                        append(it)
                    }

                    item.rating?.let {
                        if (isNotEmpty()) {
                            append("  •  ")
                        }

                        append("⭐ ")
                        append(it)
                    }
                },
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = item.type.name
                    .replace("_", " ")
                    .lowercase()
                    .replaceFirstChar {
                        it.uppercase()
                    },
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}