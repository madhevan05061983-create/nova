package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.MessageRole
import com.example.model.NovaMessage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NovaRepository(context: Context) {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(
        context.applicationContext.getString(R.string.firestore_database_id)
    )
    private val auth = Firebase.auth

    fun currentUserId(): String? = auth.currentUser?.uid

    fun observeConversations(userId: String): Flow<List<NovaMessage>> = callbackFlow {
        val collection = db.collection("users").document(userId).collection("conversations")
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val registration = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("NovaRepository", "Snapshot error", error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    try {
                        val roleStr = doc.getString("role") ?: "ASSISTANT"
                        val role = if (roleStr == "USER") MessageRole.USER else MessageRole.ASSISTANT
                        NovaMessage(
                            id = doc.id,
                            role = role,
                            content = doc.getString("content") ?: "",
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                            actionUrl = doc.getString("actionUrl"),
                            actionLabel = doc.getString("actionLabel"),
                            intent = doc.getString("intent")
                        )
                    } catch (_: Exception) {
                        null
                    }
                }
                trySend(list)
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun saveConversationMessage(userId: String, message: NovaMessage) {
        try {
            val docRef = db.collection("users").document(userId)
                .collection("conversations").document(message.id)

            val data = hashMapOf(
                "id" to message.id,
                "role" to message.role.name,
                "content" to message.content,
                "timestamp" to message.timestamp,
                "actionUrl" to (message.actionUrl ?: ""),
                "actionLabel" to (message.actionLabel ?: ""),
                "intent" to (message.intent ?: "general"),
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data).await()
        } catch (e: Exception) {
            Log.e("NovaRepository", "Failed to save message to Firestore", e)
        }
    }

    suspend fun clearHistory(userId: String) {
        try {
            val docs = db.collection("users").document(userId)
                .collection("conversations").get().await()
            for (doc in docs.documents) {
                doc.reference.delete()
            }
        } catch (e: Exception) {
            Log.e("NovaRepository", "Failed to clear history", e)
        }
    }

    suspend fun saveNote(userId: String, content: String) {
        try {
            val notesRef = db.collection("users").document(userId).collection("notes")
            val data = hashMapOf(
                "content" to content,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            notesRef.add(data).await()
        } catch (e: Exception) {
            Log.e("NovaRepository", "Failed to save note", e)
        }
    }
}
