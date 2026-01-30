package com.flamematch.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flamematch.app.ui.theme.FlameRed
import com.flamematch.app.ui.theme.DarkBackground
import com.flamematch.app.ui.theme.GoldPremium
import com.flamematch.app.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToDiscover: () -> Unit,
    onNavigateToMatches: () -> Unit,
    onNavigateToLikes: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToChat: () -> Unit,
    onLogout: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val matches by viewModel.matches.collectAsState()
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DarkBackground
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.LocalFireDepartment, contentDescription = "Discover") },
                    label = { Text("Discover") },
                    selected = false,
                    onClick = onNavigateToDiscover,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FlameRed,
                        unselectedIconColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Likes") },
                    label = { Text("Likes") },
                    selected = false,
                    onClick = onNavigateToLikes,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FlameRed,
                        unselectedIconColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Matches") },
                    label = { Text("Matches") },
                    selected = false,
                    onClick = onNavigateToMatches,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FlameRed,
                        unselectedIconColor = Color.Gray
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = false,
                    onClick = onNavigateToProfile,
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = FlameRed,
                        unselectedIconColor = Color.Gray
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔥 FlameMatch",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                IconButton(onClick = onNavigateToWallet) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Premium",
                        tint = GoldPremium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Welcome, ${currentUser?.name ?: "User"}!",
                fontSize = 20.sp,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "You have ${matches.size} matches",
                fontSize = 16.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = onNavigateToDiscover,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FlameRed)
            ) {
                Icon(Icons.Default.LocalFireDepartment, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Swiping", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
