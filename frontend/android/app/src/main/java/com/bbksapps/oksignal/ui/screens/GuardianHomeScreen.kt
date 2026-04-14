package com.bbksapps.oksignal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.bbksapps.oksignal.R
import com.bbksapps.oksignal.ui.components.EmptyMemberCard
import com.bbksapps.oksignal.ui.components.GuardianMemberCard
import com.bbksapps.oksignal.ui.theme.Dimens
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.alpha

data class GuardianMemberUiModel(
    val displayName: String,
    val lastActive: String,
    val lastLocation: String?,
    val isActive: Boolean
)

@Composable
fun GuardianHomeScreen(
    onMemberClick: (GuardianMemberUiModel) -> Unit = {},
    onInviteClick: () -> Unit = {}
) {
    val mockMembers = listOf(
        GuardianMemberUiModel(
            displayName = "엄마",
            lastActive = "Apr 13, 2026 13:25",
            lastLocation = null,
            isActive = true
        ),
        GuardianMemberUiModel(
            displayName = "할아버지",
            lastActive = "Apr 13, 2026 10:14",
            lastLocation = "103.29, 37.5665",
            isActive = false
        ),
        GuardianMemberUiModel(
            displayName = "할머니",
            lastActive = "Apr 13, 2026 12:01",
            lastLocation = null,
            isActive = true
        ),
        GuardianMemberUiModel(
            displayName = "아빠",
            lastActive = "Apr 13, 2026 08:42",
            lastLocation = "127.02, 37.49",
            isActive = false
        ),
        GuardianMemberUiModel(
            displayName = "삼촌",
            lastActive = "Apr 13, 2026 14:10",
            lastLocation = null,
            isActive = true
        )
    )

    val pages: List<List<GuardianMemberUiModel?>> = mockMembers
        .chunked(4)
        .map { chunk -> chunk + List(4 - chunk.size) { null } }
        .ifEmpty { listOf(List(4) { null }) }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6))
            .padding(Dimens.SpaceLg)
    ) {
        Text(
            text = stringResource(R.string.guardian_home_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .padding(top = Dimens.SpaceMd)
        ) { pageIndex ->
            GuardianPageContent(
                items = pages[pageIndex],
                onMemberClick = onMemberClick,
                onInviteClick = onInviteClick
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = Dimens.PagerIndicatorTopPadding,
                    bottom = Dimens.SpaceMd
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val canGoPrevious = pagerState.currentPage > 0
            val canGoNext = pagerState.currentPage < pages.lastIndex

            IconButton(
                onClick = {
                    if (canGoPrevious) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                },
                enabled = canGoPrevious,
                modifier = Modifier
                    .size(Dimens.PagerArrowButtonSize)
                    .alpha(if (canGoPrevious) 1f else 0.3f)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = stringResource(R.string.previous_page)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.PagerIndicatorSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(Dimens.PagerIndicatorSize)
                            .background(
                                color = if (index == pagerState.currentPage) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                                },
                                shape = CircleShape
                            )
                    )
                }
            }

            IconButton(
                onClick = {
                    if (canGoNext) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                enabled = canGoNext,
                modifier = Modifier
                    .size(Dimens.PagerArrowButtonSize)
                    .alpha(if (canGoNext) 1f else 0.3f)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = stringResource(R.string.next_page)
                )
            }
        }
    }
}

@Composable
private fun GuardianPageContent(
    items: List<GuardianMemberUiModel?>,
    onMemberClick: (GuardianMemberUiModel) -> Unit,
    onInviteClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Dimens.GuardianGridGap)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(Dimens.GuardianGridGap)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                items[0]?.let { item ->
                    GuardianMemberCard(
                        displayName = item.displayName,
                        lastActive = item.lastActive,
                        lastLocation = item.lastLocation,
                        isActive = item.isActive,
                        onClick = { onMemberClick(item) }
                    )
                } ?: EmptyMemberCard(onClick = onInviteClick)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                items[1]?.let { item ->
                    GuardianMemberCard(
                        displayName = item.displayName,
                        lastActive = item.lastActive,
                        lastLocation = item.lastLocation,
                        isActive = item.isActive,
                        onClick = { onMemberClick(item) }
                    )
                } ?: EmptyMemberCard(onClick = onInviteClick)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(Dimens.GuardianGridGap)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                items[2]?.let { item ->
                    GuardianMemberCard(
                        displayName = item.displayName,
                        lastActive = item.lastActive,
                        lastLocation = item.lastLocation,
                        isActive = item.isActive,
                        onClick = { onMemberClick(item) }
                    )
                } ?: EmptyMemberCard(onClick = onInviteClick)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                items[3]?.let { item ->
                    GuardianMemberCard(
                        displayName = item.displayName,
                        lastActive = item.lastActive,
                        lastLocation = item.lastLocation,
                        isActive = item.isActive,
                        onClick = { onMemberClick(item) }
                    )
                } ?: EmptyMemberCard(onClick = onInviteClick)
            }
        }
    }
}