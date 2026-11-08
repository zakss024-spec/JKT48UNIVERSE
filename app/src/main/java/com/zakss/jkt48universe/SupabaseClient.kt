package com.zakss.jkt48universe

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {

    val client = createSupabaseClient(
        supabaseUrl = "https://viujkoticyusrilcigar.supabase.co",
        supabaseKey = "sb_publishable_c_4CU5acrEVqb4W4bLhY1Q_J60sSe4C"
    ) {
        install(Auth)
        install(Postgrest)
    }
}
