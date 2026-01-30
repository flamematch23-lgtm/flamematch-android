package com.flamematch.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.flamematch.app.data.model.User
import com.flamematch.app.ui.theme.FlameRed
import com.flamematch.app.ui.theme.FlameOrange
import com.flamematch.app.ui.theme.DarkBackground
import com.flamematch.app.viewmodel.MainViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val users by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showMatch by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadUsersToDiscover()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discover", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = FlameRed
                )
            } else if (users.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔍", fontSize = 60.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No more profiles nearby",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    Text(
                        "Check back later!",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                val currentUser = users.firstOrNull()
                if (currentUser != null) {
                    SwipeableCard(
                        user = currentUser,
                        onSwipeLeft = { viewModel.passUser(currentUser.id) },
                        onSwipeRight = {
                            viewModel.likeUser(currentUser.id) { match ->
                                showMatch = true
                            }
                        }
                    )
                    
                    // Action buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(32.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FloatingActionButton(
                            onClick = { viewModel.passUser(currentUser.id) },
                            containerColor = Color.White
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Pass", tint = Color.Red)
                        }
                        
                        FloatingActionButton(
                            onClick = {
                                viewModel.likeUser(currentUser.id) { match ->
                                    showMatch = true
                                }
                            },
                            containerColor = FlameRed
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = "Like", tint = Color.White)
                        }
                    }
                }
            }
            
            if (showMatch) {
                AlertDialog(
                    onDismissRequest = { showMatch = false },
                    title = { Text("It's a Match! 🎉") },
                    text = { Text("You and this person liked each other!") },
                    confirmButton = {
                        TextButton(onClick = { showMatch = false }) {
                            Text("Keep Swiping")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SwipeableCard(
    user: User,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .padding(16.dp)
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .graphicsLayer {
                rotationZ = offsetX / 50
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        when {
                            offsetX > 200 -> onSwipeRight()
                            offsetX < -200 -> onSwipeLeft()
                        }
                        offsetX = 0f
                    }
                ) { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                }
            },
        shape = RoundedCornerShape(16.dp)
    ) {
        Box {
            if (user.photos.isNotEmpty()) {
                AsyncImage(
                    model = user.photos.first(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👤", fontSize = 80.sp)
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${user.name}, ${user.age}",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (user.isVerified) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = Color.Cyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (user.location.isNotEmpty()) {
                        Text(
                            text = "📍 ${user.location}",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                    if (user.bio.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = user.bio,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}
