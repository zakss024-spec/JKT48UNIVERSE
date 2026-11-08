package com.zakss.jkt48universe

import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MemberProfile(
    val id: Long,
    val name: String,
    val nickname: String? = null,
    val generation: Int? = null,
    val status: String,
    val team: String? = null,

    @SerialName("birth_date")
    val birthDate: String? = null,

    val height: Int? = null,

    @SerialName("blood_type")
    val bloodType: String? = null,

    @SerialName("graduation_date")
    val graduationDate: String? = null,

    @SerialName("photo_url")
    val photoUrl: String? = null,

    @SerialName("photo_credit")
    val photoCredit: String? = null,

    val bio: String? = null,

    @SerialName("source_url")
    val sourceUrl: String? = null
)

object MemberRepository {

    suspend fun getAllMembers(): List<MemberProfile> {
        return SupabaseClient.client
            .from("members")
            .select()
            .decodeList<MemberProfile>()
    }

    suspend fun getMembersByStatus(
        status: String
    ): List<MemberProfile> {
        return getAllMembers().filter {
            it.status == status
        }
    }
}
