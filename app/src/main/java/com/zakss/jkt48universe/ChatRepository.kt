package com.zakss.jkt48universe

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class NewChatMessage(
    @SerialName("sender_id") val senderId: String,
    val body: String
)

object ChatRepository {
    suspend fun currentUserId(): String? =
        SupabaseClient.client.auth.currentSessionOrNull()?.user?.id

    suspend fun getLatestMessages(): List<ChatMessage> =
        SupabaseClient.client
            .from("chat_messages")
            .select()
            .decodeList<ChatMessage>()
            .sortedBy { it.id }
            .takeLast(100)

    suspend fun sendMessage(body: String) {
        val cleanBody = body.trim()
        require(cleanBody.isNotEmpty()) { "Pesan tidak boleh kosong." }
        require(cleanBody.length <= 500) { "Pesan maksimal 500 karakter." }

        val userId = currentUserId()
            ?: throw IllegalStateException("Sesi login tidak ditemukan. Login ulang.")

        SupabaseClient.client
            .from("chat_messages")
            .insert(NewChatMessage(senderId = userId, body = cleanBody))
    }
}
