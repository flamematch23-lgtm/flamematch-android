package com.flamematch.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flamematch.app.data.model.User
import com.flamematch.app.data.model.Match
import com.flamematch.app.data.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser
    
    private val _isLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn
    
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users
    
    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches
    
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private var messagesListener: ListenerRegistration? = null
    
    init {
        if (auth.currentUser != null) {
            loadCurrentUser()
            loadMatches()
        }
    }
    
    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _isLoggedIn.value = true
                loadCurrentUser()
                loadMatches()
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun register(email: String, password: String, name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val userId = result.user?.uid ?: return@launch
                
                val newUser = User(
                    id = userId,
                    email = email,
                    name = name
                )
                
                db.collection("users").document(userId).set(newUser).await()
                _currentUser.value = newUser
                _isLoggedIn.value = true
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun logout() {
        auth.signOut()
        _isLoggedIn.value = false
        _currentUser.value = null
        _matches.value = emptyList()
        messagesListener?.remove()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            try {
                val doc = db.collection("users").document(userId).get().await()
                _currentUser.value = doc.toObject(User::class.java)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun loadUsersToDiscover() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val result = db.collection("users")
                    .whereNotEqualTo("id", userId)
                    .limit(20)
                    .get()
                    .await()
                _users.value = result.toObjects(User::class.java)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun likeUser(likedUserId: String, onMatch: (Match) -> Unit) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            try {
                // Check if other user already liked us
                val existingLike = db.collection("likes")
                    .whereEqualTo("fromUserId", likedUserId)
                    .whereEqualTo("toUserId", userId)
                    .get()
                    .await()
                
                if (!existingLike.isEmpty) {
                    // It's a match!
                    val matchId = db.collection("matches").document().id
                    val match = Match(
                        id = matchId,
                        user1Id = userId,
                        user2Id = likedUserId,
                        timestamp = System.currentTimeMillis()
                    )
                    db.collection("matches").document(matchId).set(match).await()
                    onMatch(match)
                } else {
                    // Just a like
                    val likeData = mapOf(
                        "fromUserId" to userId,
                        "toUserId" to likedUserId,
                        "timestamp" to System.currentTimeMillis()
                    )
                    db.collection("likes").add(likeData).await()
                }
                
                // Remove user from list
                _users.value = _users.value.filter { it.id != likedUserId }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun passUser(passedUserId: String) {
        _users.value = _users.value.filter { it.id != passedUserId }
    }
    
    fun loadMatches() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            try {
                val result1 = db.collection("matches")
                    .whereEqualTo("user1Id", userId)
                    .get()
                    .await()
                
                val result2 = db.collection("matches")
                    .whereEqualTo("user2Id", userId)
                    .get()
                    .await()
                
                val allMatches = result1.toObjects(Match::class.java) + 
                                 result2.toObjects(Match::class.java)
                _matches.value = allMatches.sortedByDescending { it.timestamp }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun loadMessages(matchId: String) {
        messagesListener?.remove()
        messagesListener = db.collection("messages")
            .whereEqualTo("matchId", matchId)
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _error.value = error.message
                    return@addSnapshotListener
                }
                _messages.value = snapshot?.toObjects(Message::class.java) ?: emptyList()
            }
    }
    
    fun sendMessage(matchId: String, text: String) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            try {
                val messageId = db.collection("messages").document().id
                val message = Message(
                    id = messageId,
                    matchId = matchId,
                    senderId = userId,
                    text = text,
                    timestamp = System.currentTimeMillis()
                )
                db.collection("messages").document(messageId).set(message).await()
                
                // Update match last message
                db.collection("matches").document(matchId).update(
                    mapOf(
                        "lastMessage" to text,
                        "lastMessageTime" to System.currentTimeMillis()
                    )
                ).await()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun updateProfile(user: User) {
        viewModelScope.launch {
            try {
                db.collection("users").document(user.id).set(user).await()
                _currentUser.value = user
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        messagesListener?.remove()
    }
}
