package com.example.zapzap.util

/**
 * Constantes globais do aplicativo.
 */
object Constants {
    // Supabase (Adicionado para Storage gratuito)
    const val SUPABASE_URL = "https://qfpzmfolingyuqcbpism.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFmcHptZm9saW5neXVxY2JwaXNtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzM2MjgwMzQsImV4cCI6MjA4OTIwNDAzNH0.6RwIONT4UPDt15cPGVLZEa3RBSVSt_YQHbuNyFLWDn8"

    // Firebase Collections
    const val USERS_COLLECTION = "users"
    const val CONVERSATIONS_COLLECTION = "conversations"
    const val MESSAGES_COLLECTION = "messages"
    const val CONTACTS_COLLECTION = "contacts"

    // Storage Buckets
    const val STORAGE_IMAGES = "images"
    const val STORAGE_VIDEOS = "videos"
    const val STORAGE_AUDIOS = "audios"
    const val STORAGE_FILES = "files"
    const val STORAGE_PROFILE_PHOTOS = "profile_photos"

    // Notification
    const val NOTIFICATION_CHANNEL_ID = "zapzap_messages"

    // Request Codes
    const val REQUEST_CAMERA = 100
    const val REQUEST_LOCATION = 101
    const val REQUEST_AUDIO = 102
    const val REQUEST_CONTACTS = 103
    const val REQUEST_NOTIFICATIONS = 104
    const val REQUEST_STORAGE = 105

    // Audio
    const val MAX_AUDIO_DURATION_MS = 120_000L // 2 minutos
}
