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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.bbksapps.oksignal.data.repository.InviteRepository
import android.widget.Toast
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import retrofit2.HttpException
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import android.content.Intent
import androidx.compose.runtime.LaunchedEffect
import com.bbksapps.oksignal.data.repository.GuardianRepository
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api

data class GuardianMemberUiModel(
    val displayName: String,
    val lastActive: String,
    val lastLocation: String?,
    val isActive: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuardianHomeScreen(
    guardianUserId: String,
    onMemberClick: (GuardianMemberUiModel) -> Unit = {},
    onLogout: () -> Unit
) {
    /*val mockMembers = listOf(
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

     */

    var showAccountMenu by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val inviteRepository = remember { InviteRepository() }
    val guardianRepository = remember { GuardianRepository() }

    var members by remember { mutableStateOf<List<GuardianMemberUiModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var inviteDialogIsError by remember { mutableStateOf(false) }

    var inviteResultText by remember { mutableStateOf<String?>(null) }
    var inviteDialogText by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val pages: List<List<GuardianMemberUiModel?>> = members
        .chunked(4)
        .map { chunk -> chunk + List(4 - chunk.size) { null } }
        .ifEmpty { listOf(List(4) { null }) }

    val pagerState = rememberPagerState(pageCount = { pages.size })

    fun shareInviteLink(link: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, link)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, context.getString(R.string.share_invite_link))
        context.startActivity(shareIntent)
    }

    LaunchedEffect(guardianUserId) {
        try {
            isLoading = true
            loadError = null

            val response = guardianRepository.getGuardianMembers(guardianUserId)
            if (response.success) {
                members = response.members.map { dto ->
                    GuardianMemberUiModel(
                        displayName = dto.member_display_name ?: context.getString(R.string.unknown_member),
                        lastActive = dto.last_activity_at ?: context.getString(R.string.no_activity),
                        lastLocation = if (dto.last_known_lat != null && dto.last_known_lng != null) {
                            "${dto.last_known_lng}, ${dto.last_known_lat}"
                        } else null,
                        isActive = dto.last_activity_at != null
                    )
                }
            } else {
                loadError = response.error ?: context.getString(R.string.load_members_error)
            }
        } catch (e: Exception) {
            loadError = e.message ?: context.getString(R.string.unknown_error)
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OKSignal") },
                actions = {
                    Box {
                        IconButton(onClick = { showAccountMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Account"
                            )
                        }

                        DropdownMenu(
                            expanded = showAccountMenu,
                            onDismissRequest = { showAccountMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Logout") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Logout,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    showAccountMenu = false
                                    showLogoutDialog = true
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF3F4F6))
                .padding(innerPadding)
                .padding(Dimens.SpaceLg)
        ) {
            Text(
                text = stringResource(R.string.guardian_home_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (isLoading) {
                Text(
                    text = stringResource(R.string.loading_members),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = Dimens.SpaceSm)
                )
            }

            loadError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = Dimens.SpaceSm)
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = Dimens.SpaceMd)
            ) { pageIndex ->
                GuardianPageContent(
                    items = pages[pageIndex],
                    onMemberClick = onMemberClick,
                    onInviteClick = {
                        coroutineScope.launch {
                            try {
                                val response = inviteRepository.createInvite(guardianUserId)

                                if (response.success && !response.invite_link.isNullOrBlank()) {
                                    inviteResultText = response.invite_link
                                    inviteDialogText = response.invite_link
                                    inviteDialogIsError = false
                                } else {
                                    inviteResultText = response.error ?: context.getString(R.string.invite_failed)
                                    inviteDialogText = response.error ?: context.getString(R.string.invite_failed)
                                    inviteDialogIsError = true
                                }
                            } catch (e: HttpException) {
                                val errorBody = e.response()?.errorBody()?.string()
                                val message = context.getString(
                                    R.string.http_error,
                                    e.code(),
                                    errorBody ?: e.message()
                                )
                                inviteResultText = message
                                inviteDialogText = message
                                inviteDialogIsError = true
                            } catch (e: Exception) {
                                inviteResultText = e.message ?: context.getString(R.string.unknown_error)
                                inviteDialogText = e.message ?: context.getString(R.string.unknown_error)
                                inviteDialogIsError = true
                            }
                        }
                    }
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

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign out?") },
            text = {
                Text("You’ll need to sign in again to access your OKSignal account.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text("Sign out")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    inviteDialogText?.let { dialogText ->
        AlertDialog(
            onDismissRequest = { inviteDialogText = null },
            title = {
                Text(
                    if (inviteDialogIsError) {
                        stringResource(R.string.invite_error_title)
                    } else {
                        stringResource(R.string.invite_link_title)
                    }
                )
            },
            text = {
                if (inviteDialogIsError) {
                    Text(dialogText)
                } else {
                    Column {
                        Text(stringResource(R.string.invite_link_ready))
                        Spacer(modifier = Modifier.height(Dimens.SpaceSm))
                        Text(stringResource(R.string.invite_link_instruction))
                    }
                }
            },
            confirmButton = {
                if (inviteDialogIsError) {
                    TextButton(onClick = { inviteDialogText = null }) {
                        Text(stringResource(R.string.ok))
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                        TextButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(dialogText))
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.invite_link_copied),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        ) {
                            Text(stringResource(R.string.copy))
                        }

                        TextButton(
                            onClick = { shareInviteLink(dialogText) }
                        ) {
                            Text(stringResource(R.string.share))
                        }

                        TextButton(
                            onClick = { inviteDialogText = null }
                        ) {
                            Text(stringResource(R.string.close))
                        }
                    }
                }
            }
        )
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