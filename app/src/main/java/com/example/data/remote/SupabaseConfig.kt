package com.example.data.remote

data class SupabaseConfig(
    val url: String = "https://example-school-db.supabase.co",
    val anonKey: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    val isConnected: Boolean = true
) {
    val isValid: Boolean get() = url.startsWith("http://") || url.startsWith("https://")
}
