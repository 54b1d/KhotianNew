package com.sabid.khotianv2.data.repository

import com.sabid.khotianv2.domain.repository.KhotianRepository
import io.github.jan.supabase.SupabaseClient
import javax.inject.Inject

class KhotianRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : KhotianRepository {
    // Implement domain repository methods here
}