@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.darach.plcards.ui.common

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import com.darach.plcards.domain.model.CardModel

@Composable
fun CardItem(
    cardModel: CardModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null,
    sharedElementKey: String? = null
) {
    val isTablet = windowSizeClass?.widthSizeClass != WindowWidthSizeClass.Compact
    val cardWidth = if (isTablet) {
        when(windowSizeClass?.widthSizeClass) {
            WindowWidthSizeClass.Medium -> 180.dp
            WindowWidthSizeClass.Expanded -> 200.dp
            else -> 160.dp
        }
    } else 160.dp
    val cardHeight = if (isTablet) {
        when(windowSizeClass?.widthSizeClass) {
            WindowWidthSizeClass.Medium -> 350.dp
            WindowWidthSizeClass.Expanded -> 390.dp
            else -> 310.dp
        }
    } else 310.dp
    
    Card(
        modifier = modifier
            .width(cardWidth)
            .height(cardHeight)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 12.dp, bottomEnd = 12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth().background(Color.White)
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(cardModel.cardImageUrl)
                        .crossfade(true)
                        .allowHardware(true)
                        .build(),
                    contentDescription = cardModel.playerName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 0.dp, bottomEnd = 0.dp))
                        .then(
                            if (sharedTransitionScope != null && animatedContentScope != null) {
                                with(sharedTransitionScope) {
                                    Modifier.sharedElement(
                                        sharedContentState = rememberSharedContentState(
                                            key = sharedElementKey ?: "card-image-${cardModel.id}"
                                        ),
                                        animatedVisibilityScope = animatedContentScope
                                    )
                                }
                            } else Modifier
                        )
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = cardModel.playerName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = cardModel.team,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = cardModel.season,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}