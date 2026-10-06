package com.bebetter.bemora.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
fun ContentCard(item: ContentItem) {
    Column(
        modifier = Modifier.width(160.dp)
    ) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = "Poster for ${item.title}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = item.title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = "${item.releaseYear ?: ""}  •  ⭐ ${item.rating ?: ""}",
            color = Color.Gray,
            fontSize = 13.sp
        )
    }
}